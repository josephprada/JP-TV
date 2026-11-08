package co.jp.tv.apps.domain

/** An installed app that can be launched from the home screen. */
data class TvApp(
    val packageName: String,
    val label: String,
    /** True when the app declares a LEANBACK_LAUNCHER activity. */
    val isTvApp: Boolean,
)
