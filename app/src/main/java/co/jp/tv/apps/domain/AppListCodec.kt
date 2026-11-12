package co.jp.tv.apps.domain

/** Plain-text snapshot of the app list: one `package<TAB>tv<TAB>label` line per app. */
object AppListCodec {
    private const val SEP = '\t'

    fun encode(apps: List<TvApp>): String = apps.joinToString("\n") { app ->
        val label = app.label.replace(SEP, ' ').replace('\n', ' ').replace('\r', ' ')
        "${app.packageName}$SEP${if (app.isTvApp) 1 else 0}$SEP$label"
    }

    /** Lines that do not parse are dropped; a damaged cache never throws. */
    fun decode(text: String): List<TvApp> = text.lineSequence().mapNotNull { line ->
        val parts = line.split(SEP, limit = 3)
        if (parts.size != 3 || parts[0].isEmpty() || parts[1] !in setOf("0", "1")) null
        else TvApp(parts[0], parts[2], isTvApp = parts[1] == "1")
    }.toList()
}
