package co.jp.tv.homeoverride.data

import android.content.Context
import android.util.Log
import co.jp.tv.homeoverride.domain.RedirectPolicy

/** "Abrir launcher original": starts the stock home on purpose and gives the user a grace period away from JP-TV. */
class StockLauncherOpener(
    context: Context,
    private val resolver: StockHomeResolver,
    private val policy: RedirectPolicy,
) {
    private val appContext = context.applicationContext

    fun open(): Boolean {
        policy.startGrace()
        for (entry in resolver.entries()) {
            try {
                appContext.startActivity(entry.intent())
                return true
            } catch (e: RuntimeException) {
                Log.w(TAG, "Could not open ${entry.component}", e)
            }
        }
        return false
    }

    private companion object {
        const val TAG = "StockLauncherOpener"
    }
}
