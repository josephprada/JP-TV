package co.jp.tv.homeoverride.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import co.jp.tv.homeoverride.domain.StockHomes

/** Adapter: asks PackageManager which activities act as home (MAIN+HOME and the OEM's HOME.JM variant). */
class StockHomeResolver(context: Context) {
    private val appContext = context.applicationContext

    /** A stock home activity and the category that starts it. */
    data class Entry(val component: ComponentName, val category: String) {
        fun intent(): Intent = Intent(Intent.ACTION_MAIN).addCategory(category).setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /** Stock home activities except JP-TV and the never-touch packages, the known launchers first. */
    fun entries(): List<Entry> {
        val own = appContext.packageName
        val found = CATEGORIES.flatMap { category ->
            appContext.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)
                .mapNotNull { it.activityInfo }
                .map { Entry(ComponentName(it.packageName, it.name), category) }
        }.distinctBy { it.component }
        val allowed = StockHomes.candidates(found.map { it.component.packageName }.toSet(), own)
        return found.filter { it.component.packageName in allowed }
            .sortedBy { StockHomes.KNOWN.indexOf(it.component.packageName).let { i -> if (i < 0) Int.MAX_VALUE else i } }
    }

    fun packages(): Set<String> = entries().map { it.component.packageName }.toSet()

    companion object {
        const val CATEGORY_HOME_JM = "android.intent.category.HOME.JM"
        private val CATEGORIES = listOf(Intent.CATEGORY_HOME, CATEGORY_HOME_JM)
    }
}
