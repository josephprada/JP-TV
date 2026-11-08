package co.jp.tv.apps.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class AppLayoutTest {
    private fun app(pkg: String, label: String, tv: Boolean) = TvApp(pkg, label, tv)

    @Test
    fun `tv apps come first then the rest alphabetically`() {
        val result = AppLayout.arrange(
            apps = listOf(
                app("a.calc", "Calculadora", false),
                app("a.yt", "YouTube", true),
                app("a.nf", "Netflix", true),
                app("a.cam", "Camara", false),
            ),
            ownPackage = "co.jp.tv",
        )
        assertEquals(listOf("Netflix", "YouTube", "Calculadora", "Camara"), result.map { it.label })
    }

    @Test
    fun `sorting ignores case and is stable on equal labels via package name`() {
        val result = AppLayout.arrange(
            apps = listOf(
                app("b.app", "plex", true),
                app("z.app", "Plex", true),
                app("a.app", "Plex", true),
                app("c.app", "Ajustes", true),
            ),
            ownPackage = "co.jp.tv",
        )
        assertEquals(listOf("c.app", "a.app", "b.app", "z.app"), result.map { it.packageName })
    }

    @Test
    fun `excludes our own package`() {
        val result = AppLayout.arrange(
            apps = listOf(app("co.jp.tv", "JP-TV", true), app("a.nf", "Netflix", true)),
            ownPackage = "co.jp.tv",
        )
        assertEquals(listOf("a.nf"), result.map { it.packageName })
    }

    @Test
    fun `empty input gives empty output`() {
        assertEquals(emptyList<TvApp>(), AppLayout.arrange(emptyList(), "co.jp.tv"))
    }

    @Test
    fun `first run puts installed favorites first in their own order`() {
        val default = listOf("a.calc", "com.spotify.tv.android", "com.netflix.mediaclient", "com.xuper.netxxus", "a.zip")
        val result = AppLayout.firstRun(default)
        assertEquals(
            listOf("com.xuper.netxxus", "com.netflix.mediaclient", "com.spotify.tv.android", "a.calc", "a.zip"),
            result.order,
        )
    }

    @Test
    fun `first run hides only the system and utility packages that are installed`() {
        val default = listOf("com.android.vending", "com.droidlogic.tv.settings", "a.nf", "com.droidlogic.otaupgrade")
        val result = AppLayout.firstRun(default)
        assertEquals(setOf("com.droidlogic.tv.settings", "com.droidlogic.otaupgrade"), result.hidden)
        assertEquals(listOf("com.android.vending", "a.nf"), result.visible)
    }

    @Test
    fun `first run without favorites keeps the default order`() {
        assertEquals(listOf("a", "b"), AppLayout.firstRun(listOf("a", "b")).order)
    }

    @Test
    fun `resolve returns visible and hidden apps in saved order and drops unknown packages`() {
        val installed = listOf(app("a", "A", true), app("b", "B", true), app("c", "C", true))
        val saved = AppOrdering(order = listOf("c", "gone", "b", "a"), hidden = setOf("a", "gone"))
        val result = AppLayout.resolve(installed, saved)
        assertEquals(listOf("c", "b"), result.visible.map { it.packageName })
        assertEquals(listOf("a"), result.hidden.map { it.packageName })
    }
}
