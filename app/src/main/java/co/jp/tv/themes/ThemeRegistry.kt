package co.jp.tv.themes

import co.jp.tv.themes.cartelera.CarteleraTheme
import co.jp.tv.themes.cyberpunk.CyberpunkTheme
import co.jp.tv.themes.gotham.GothamTheme
import co.jp.tv.themes.hud.HudTheme
import co.jp.tv.themes.jptv.JpTvTheme
import co.jp.tv.themes.lofi.LofiTheme
import co.jp.tv.themes.multiverso.MultiversoTheme
import co.jp.tv.themes.oceano.OceanoTheme
import co.jp.tv.themes.pixel.PixelTheme
import co.jp.tv.themes.terminal.TerminalTheme
import co.jp.tv.themes.vinilo.ViniloTheme
import co.jp.tv.themes.zen.ZenTheme

/**
 * The single place where themes are registered.
 *
 * Entries are lazy: [byId] only loads the class of the theme that is asked for, so a cold start does not pull in
 * the twelve themes; [all] (the picker) loads them on first use.
 */
object ThemeRegistry {
    const val DEFAULT_ID = "jptv"

    private class Entry(val id: String, create: () -> LauncherTheme) {
        val theme: LauncherTheme by lazy(create)
    }

    private val entries = listOf(
        Entry("jptv") { JpTvTheme },
        Entry("pixel") { PixelTheme },
        Entry("terminal") { TerminalTheme },
        Entry("cartelera") { CarteleraTheme },
        Entry("zen") { ZenTheme },
        Entry("hud") { HudTheme },
        Entry("vinilo") { ViniloTheme },
        Entry("lofi") { LofiTheme },
        Entry("gotham") { GothamTheme },
        Entry("multiverso") { MultiversoTheme },
        Entry("cyberpunk") { CyberpunkTheme },
        Entry("oceano") { OceanoTheme },
    )

    val all: List<LauncherTheme> by lazy { entries.map { it.theme } }

    fun byId(id: String): LauncherTheme = (entries.firstOrNull { it.id == id } ?: entries.first { it.id == DEFAULT_ID }).theme

    val default: LauncherTheme get() = byId(DEFAULT_ID)
}
