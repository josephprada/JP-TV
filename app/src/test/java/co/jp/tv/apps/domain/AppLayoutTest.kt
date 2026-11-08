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
}
