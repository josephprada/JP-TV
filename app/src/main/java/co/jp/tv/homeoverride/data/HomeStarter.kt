package co.jp.tv.homeoverride.data

import android.content.Context
import android.content.Intent
import android.util.Log
import co.jp.tv.MainActivity

/** Brings JP-TV's home to the front as the HOME intent would, so MainActivity.onNewIntent closes the open panels. */
class HomeStarter(context: Context) {
    private val appContext = context.applicationContext

    /** [from] is the context that starts the activity; the accessibility service is the best one to use. */
    fun start(from: Context = appContext): Boolean = try {
        from.startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setClassName(appContext.packageName, MainActivity::class.java.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
        )
        true
    } catch (e: RuntimeException) {
        Log.w(TAG, "Could not start the home", e)
        false
    }

    private companion object {
        const val TAG = "HomeStarter"
    }
}
