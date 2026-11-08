package co.jp.tv.apps.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.TvApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/** Adapter: lists LEANBACK_LAUNCHER and LAUNCHER activities and refreshes on package changes. */
class PackageManagerAppCatalog(private val context: Context) : AppCatalog {

    private val pm: PackageManager = context.packageManager

    override val apps: Flow<List<TvApp>> = packageChanges()
        .conflate()
        .map { query() }
        .flowOn(Dispatchers.IO)

    override fun launch(app: TvApp) {
        val intent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
            ?: pm.getLaunchIntentForPackage(app.packageName)
        if (intent == null) {
            Log.w(TAG, "No launch intent for ${app.packageName}")
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not launch ${app.packageName}", e)
        }
    }

    private fun query(): List<TvApp> {
        val tvPackages = launchablePackages(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val byPackage = LinkedHashMap<String, TvApp>()
        for ((pkg, label) in tvPackages) byPackage[pkg] = TvApp(pkg, label, isTvApp = true)
        for ((pkg, label) in launchablePackages(Intent.CATEGORY_LAUNCHER)) {
            byPackage.getOrPut(pkg) { TvApp(pkg, label, isTvApp = false) }
        }
        return byPackage.values.toList()
    }

    private fun launchablePackages(category: String): List<Pair<String, String>> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return pm.queryIntentActivities(intent, 0).map {
            it.activityInfo.packageName to it.loadLabel(pm).toString()
        }
    }

    /** Emits once on collection, then on every package add/remove/change. */
    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)
        trySend(Unit)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private companion object {
        const val TAG = "PackageManagerAppCatalog"
    }
}
