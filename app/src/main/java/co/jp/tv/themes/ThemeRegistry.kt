package co.jp.tv.themes

import co.jp.tv.themes.jptv.JpTvTheme

/** The single place where themes are registered. */
object ThemeRegistry {
    const val DEFAULT_ID = "jptv"

    val all: List<LauncherTheme> = listOf(
        JpTvTheme,
    )

    fun byId(id: String): LauncherTheme = all.firstOrNull { it.id == id } ?: default

    val default: LauncherTheme get() = all.first { it.id == DEFAULT_ID }
}
