package co.jp.tv.homeoverride.data

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import co.jp.tv.JpTvApp
import co.jp.tv.homeoverride.domain.Decision
import co.jp.tv.homeoverride.domain.RedirectPolicy
import co.jp.tv.homeoverride.domain.StockHomes
import co.jp.tv.homeoverride.domain.Trigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Opt-in service that lets JP-TV act as home on firmwares that ignore the preferred home. It reads no
 * content (canRetrieveWindowContent is off): it only sees the Home key, where the platform delivers it,
 * and the package of the window that came to the front. It never touches the stock launcher.
 */
class HomeOverrideService : AccessibilityService() {
    private val container get() = (application as JpTvApp).container
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile private var enabled = false
    @Volatile private var stockPackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        current = this
        stockPackages = container.stockHomeResolver.packages()
        container.redirectPolicy.evaluatePending()
        scope.launch { container.configStore.config.collect { enabled = it?.homeOverrideEnabled == true } }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString()
        if (!StockHomes.isStockHome(packageName, stockPackages, this.packageName)) return
        redirect(Trigger.STOCK_WINDOW)
    }

    /** Where the platform delivers the Home key here, JP-TV takes it; elsewhere the window events above cover it. */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_HOME) return false
        if (container.redirectPolicy.decide(enabled, Trigger.HOME_KEY) !is Decision.Redirect) return false
        if (event.action == KeyEvent.ACTION_UP) redirect(Trigger.HOME_KEY)
        return true
    }

    private fun redirect(trigger: Trigger) {
        val policy = container.redirectPolicy
        if (policy.decide(enabled, trigger) !is Decision.Redirect) return
        if (!container.homeStarter.start(this)) return
        policy.onRedirected()
        handler.postDelayed({ policy.evaluatePending() }, RedirectPolicy.DRAW_TIMEOUT_MS + 250)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (current === this) current = null
        handler.removeCallbacksAndMessages(null)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        /** The connected instance, the best context to start the home from. */
        @Volatile var current: HomeOverrideService? = null
            private set
    }
}
