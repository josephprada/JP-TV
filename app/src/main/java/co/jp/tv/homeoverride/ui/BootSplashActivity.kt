package co.jp.tv.homeoverride.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.UserManager
import android.util.Log
import androidx.core.content.ContextCompat
import co.jp.tv.MainActivity
import co.jp.tv.homeoverride.domain.BootSplashPlan
import co.jp.tv.homeoverride.domain.SplashStep

/**
 * Direct-boot aware entry that draws the instant JP-TV splash (window background only) while the user's
 * credential-encrypted storage is still locked, which is when the HY320's JmLauncher starts it after power-on.
 * Once the user unlocks it forwards to [MainActivity]; after [BootSplashPlan.TIMEOUT_MS] it opens the stock home.
 *
 * It must never crash and must not touch credential-encrypted storage, so no Compose, no AppContainer, no DataStore.
 */
class BootSplashActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var startedAt = 0L
    private var stockTried = false
    private var done = false
    private var receiverRegistered = false

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = check()
    }
    private val poll = object : Runnable {
        override fun run() {
            check()
            if (!done) handler.postDelayed(this, POLL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startedAt = SystemClock.elapsedRealtime()
        try {
            ContextCompat.registerReceiver(
                this,
                unlockReceiver,
                IntentFilter(Intent.ACTION_USER_UNLOCKED),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiverRegistered = true
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not listen for the unlock; polling only", e)
        }
        // After registering, so an unlock that happened in between is not missed.
        check()
        if (!done) handler.postDelayed(poll, POLL_MS)
    }

    /** A second HOME intent while waiting: nothing to redo, just look again. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        check()
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    private fun check() {
        if (done) return
        try {
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            when (BootSplashPlan.decide(isUnlocked(), elapsed, stockTried)) {
                SplashStep.FORWARD -> forwardToHome()
                SplashStep.WAIT -> Unit
                SplashStep.STOCK_HOME -> openStockHome()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Boot splash failed, falling back to the stock home", e)
            runCatching { openStockHome() }
        }
    }

    private fun isUnlocked(): Boolean = getSystemService(UserManager::class.java)?.isUserUnlocked ?: false

    private fun forwardToHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setComponent(ComponentName(this, MainActivity::class.java))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION),
        )
        close()
    }

    /** Starts the first usable stock home; when none resolves the splash stays on screen (and keeps waiting for the unlock). */
    private fun openStockHome() {
        stockTried = true
        val flags = PackageManager.MATCH_DIRECT_BOOT_AWARE or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
        val homes = packageManager
            .queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), flags)
            .mapNotNull { it.activityInfo }
        val order = BootSplashPlan.fallbackOrder(homes.map { it.packageName }, packageName)
        for (pkg in order) {
            val info = homes.firstOrNull { it.packageName == pkg } ?: continue
            val started = runCatching {
                startActivity(
                    Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .setComponent(ComponentName(info.packageName, info.name))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION),
                )
            }.isSuccess
            if (started) {
                close()
                return
            }
        }
    }

    private fun close() {
        done = true
        release()
        finish()
        overridePendingTransition(0, 0)
    }

    private fun release() {
        handler.removeCallbacksAndMessages(null)
        if (receiverRegistered) {
            receiverRegistered = false
            runCatching { unregisterReceiver(unlockReceiver) }
        }
    }

    private companion object {
        const val TAG = "BootSplash"
        const val POLL_MS = 250L
    }
}
