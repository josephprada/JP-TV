package co.jp.tv.homeoverride.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import co.jp.tv.JpTvApp
import co.jp.tv.R
import co.jp.tv.homeoverride.domain.Decision
import co.jp.tv.homeoverride.domain.ForegroundTracker
import co.jp.tv.homeoverride.domain.Reason
import co.jp.tv.homeoverride.domain.RedirectPolicy
import co.jp.tv.homeoverride.domain.Trigger
import co.jp.tv.homeoverride.domain.WatcherMode
import co.jp.tv.homeoverride.domain.WatcherPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Backend for firmwares where the accessibility service cannot stay on: while the screen is on it polls
 * the usage events and brings JP-TV forward when a stock home becomes the foreground app. It reads only
 * which package came to the front, never content, and touches no system app. Idle while the screen is off
 * and while the accessibility backend is bound.
 */
class ForegroundWatcherService : Service() {
    private val container get() = (application as JpTvApp).container
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var thread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var usageStats: UsageStatsManager

    // The tracker, the event and the window are touched only on the worker thread.
    private val tracker = ForegroundTracker()
    private val event = UsageEvents.Event()
    private var lastQueryEnd = 0L
    private var stockPackages: Set<String> = emptySet()

    @Volatile private var enabled = true
    @Volatile private var screenOn = true

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            screenOn = intent.action == Intent.ACTION_SCREEN_ON
            handler.removeCallbacks(tick)
            if (screenOn) handler.post(wake) // refreshes the stock list and the window
        }
    }

    private val wake = Runnable {
        stockPackages = container.stockHomeResolver.packages()
        lastQueryEnd = 0L
        tracker.reset()
        tick.run()
    }

    private val tick = object : Runnable {
        override fun run() {
            if (!screenOn) return
            val permissions = container.watcherPermissions
            val bound = HomeOverrideService.current != null
            when (WatcherPlan.mode(enabled, permissions.usageAccess(), permissions.overlay(), screenOn, bound)) {
                WatcherMode.STOPPED -> stopSelf()
                WatcherMode.IDLE -> handler.postDelayed(this, WatcherPlan.IDLE_RECHECK_MS)
                WatcherMode.POLLING -> {
                    poll()
                    handler.postDelayed(this, WatcherPlan.POLL_MS)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!promote()) {
            stopSelf()
            return
        }
        usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        thread = HandlerThread("jptv-foreground-watcher").also { it.start() }
        handler = Handler(thread.looper)
        screenOn = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_ON).apply { addAction(Intent.ACTION_SCREEN_OFF) })
        container.redirectPolicy.evaluatePending()
        scope.launch {
            container.configStore.config.collect { config ->
                // null: the config is not loaded yet; keep running.
                if (config != null) {
                    enabled = config.homeOverrideEnabled
                    if (!enabled) stopSelf()
                }
            }
        }
        handler.post(wake)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    /** Reads the usage events of the last moments and redirects when a stock home is now in front. */
    private fun poll() {
        val now = System.currentTimeMillis()
        val events = try {
            usageStats.queryEvents(WatcherPlan.queryStart(now, lastQueryEnd), now)
        } catch (e: SecurityException) {
            stopSelf()
            return
        } ?: return
        lastQueryEnd = now
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == EVENT_RESUMED) event.packageName?.let { tracker.onResumed(it, event.timeStamp) }
        }
        if (tracker.pendingStock(stockPackages, packageName) == null) return
        redirect()
    }

    private fun redirect() {
        val policy = container.redirectPolicy
        when (val decision = policy.decide(enabled, Trigger.STOCK_WINDOW)) {
            Decision.Redirect -> {
                tracker.markHandled()
                if (container.homeStarter.start(this)) {
                    policy.onRedirected()
                    handler.postDelayed({ policy.evaluatePending() }, RedirectPolicy.DRAW_TIMEOUT_MS + 250)
                }
            }
            is Decision.PassThrough ->
                // Debounce and boot settling are short: ask again on the next poll. Anything else is a decision to stand down.
                if (decision.reason != Reason.DEBOUNCED && decision.reason != Reason.BOOTING) tracker.markHandled()
        }
    }

    @Suppress("DEPRECATION") // Notification priority is the pre-Oreo way to keep it quiet.
    private fun promote(): Boolean {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(R.string.home_override_channel), NotificationManager.IMPORTANCE_MIN).apply {
                    setShowBadge(false)
                    setSound(null, null)
                },
            )
        }
        val notification = Notification.Builder(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setChannelId(CHANNEL_ID)
            setSmallIcon(R.drawable.ic_jp_monogram)
            setContentTitle(getString(R.string.home_override_notification))
            setOngoing(true)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setPriority(Notification.PRIORITY_MIN)
        }.build()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not go foreground", e)
            false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        if (::handler.isInitialized) handler.removeCallbacksAndMessages(null)
        if (::thread.isInitialized) thread.quitSafely()
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "ForegroundWatcher"
        const val CHANNEL_ID = "jp_tv_home"
        const val NOTIFICATION_ID = 4101

        /** UsageEvents.Event.ACTIVITY_RESUMED (API 29), the same value as the older MOVE_TO_FOREGROUND. */
        const val EVENT_RESUMED = 1
    }
}
