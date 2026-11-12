package co.jp.tv.homeoverride.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import co.jp.tv.JpTvApp
import co.jp.tv.homeoverride.domain.Decision
import co.jp.tv.homeoverride.domain.Trigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * After a normal boot (BOOT_COMPLETED only, never direct boot) starts JP-TV a moment later when the
 * override is on and the guards allow it. By then the stock launcher has shown, so the system is healthy.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val container = (context.applicationContext as JpTvApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val enabled = withTimeoutOrNull(CONFIG_TIMEOUT_MS) { container.configStore.config.first() }?.homeOverrideEnabled == true
                if (container.redirectPolicy.decide(enabled, Trigger.BOOT) !is Decision.Redirect) return@launch
                delay(START_DELAY_MS)
                if (container.redirectPolicy.decide(enabled, Trigger.BOOT) !is Decision.Redirect) return@launch
                if (container.homeStarter.start(HomeOverrideService.current ?: context)) container.redirectPolicy.onRedirected()
            } catch (e: Exception) {
                Log.w(TAG, "Boot start failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "BootReceiver"
        const val START_DELAY_MS = 2_000L
        const val CONFIG_TIMEOUT_MS = 3_000L
    }
}
