package co.jp.tv.apps.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppListCodecTest {
    private val apps = listOf(
        TvApp("com.netflix.mediaclient", "Netflix", isTvApp = true),
        TvApp("org.videolan.vlc", "VLC", isTvApp = false),
    )

    @Test
    fun `a list round trips`() {
        assertEquals(apps, AppListCodec.decode(AppListCodec.encode(apps)))
    }

    @Test
    fun `an empty list round trips`() {
        assertEquals(emptyList<TvApp>(), AppListCodec.decode(AppListCodec.encode(emptyList())))
        assertEquals(emptyList<TvApp>(), AppListCodec.decode(""))
    }

    @Test
    fun `tabs and newlines in a label cannot break the format`() {
        val tricky = listOf(TvApp("a.b", "Foo\tBar\nBaz", isTvApp = true))
        assertEquals(listOf(TvApp("a.b", "Foo Bar Baz", isTvApp = true)), AppListCodec.decode(AppListCodec.encode(tricky)))
    }

    @Test
    fun `malformed lines are skipped and the rest survives`() {
        val text = "garbage\n" + AppListCodec.encode(apps) + "\nonly\ttwo\n"
        assertEquals(apps, AppListCodec.decode(text))
    }

    @Test
    fun `order is preserved`() {
        assertEquals(apps.reversed(), AppListCodec.decode(AppListCodec.encode(apps.reversed())))
    }
}
