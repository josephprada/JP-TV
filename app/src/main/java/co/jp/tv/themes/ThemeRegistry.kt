package co.jp.tv.themes

import co.jp.tv.themes.cartelera.CarteleraTheme
import co.jp.tv.themes.cyberpunk.CyberpunkTheme
import co.jp.tv.themes.gotham.GothamTheme
import co.jp.tv.themes.hud.HudTheme
import co.jp.tv.themes.jptv.JpTvTheme
import co.jp.tv.themes.lofi.LofiTheme
import co.jp.tv.themes.multiverso.MultiversoTheme
import co.jp.tv.themes.pixel.PixelTheme
import co.jp.tv.themes.terminal.TerminalTheme
import co.jp.tv.themes.vinilo.ViniloTheme
import co.jp.tv.themes.zen.ZenTheme

/** The single place where themes are registered. */
object ThemeRegistry {
    const val DEFAULT_ID = "jptv"

    val all: List<LauncherTheme> = listOf(
        JpTvTheme,
        PixelTheme,
        TerminalTheme,
        CarteleraTheme,
        ZenTheme,
        HudTheme,
        ViniloTheme,
        LofiTheme,
        GothamTheme,
        MultiversoTheme,
        CyberpunkTheme,
    )

    fun byId(id: String): LauncherTheme = all.firstOrNull { it.id == id } ?: default

    val default: LauncherTheme get() = all.first { it.id == DEFAULT_ID }
}
