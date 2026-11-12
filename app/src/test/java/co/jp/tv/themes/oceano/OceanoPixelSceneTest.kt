package co.jp.tv.themes.oceano

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OceanoPixelSceneTest {
    private fun input(tick: Int = 0, night: Float = 0f, scroll: Float = 1f, count: Int = 12) = OceanoSceneInput().apply {
        this.tick = tick
        this.night = night
        this.scroll = scroll
        focused = scroll.toInt()
        this.count = count
        initials = IntArray(count) { it }
    }

    private fun IntArray.meanLuma(): Double = sumOf { (((it shr 16) and 255) * 3 + ((it shr 8) and 255) * 6 + (it and 255)) / 10.0 } / size

    @Test
    fun `every pixel of every phase and weather is opaque`() {
        for (weather in OceanoWeather.entries) {
            val scene = OceanoPixelScene(weather)
            for (night in floatArrayOf(0f, 0.5f, 1f)) {
                scene.render(input(tick = 37, night = night, scroll = 2.4f))
                assertTrue(scene.frame.all { (it ushr 24) == 0xFF }, "$weather night=$night has a see-through pixel")
            }
        }
    }

    @Test
    fun `the same input renders the same frame and a later tick moves something`() {
        val scene = OceanoPixelScene()
        scene.render(input(tick = 10))
        val first = scene.frame.copyOf()
        scene.render(input(tick = 10))
        assertTrue(first.contentEquals(scene.frame))
        scene.render(input(tick = 22))
        assertTrue(!first.contentEquals(scene.frame))
    }

    @Test
    fun `scrolling and the app count change the frame, an empty list draws only the scene`() {
        val scene = OceanoPixelScene()
        scene.render(input(scroll = 1f))
        val a = scene.frame.copyOf()
        scene.render(input(scroll = 2f))
        assertTrue(!a.contentEquals(scene.frame))
        scene.render(input(count = 0))
        val empty = scene.frame.copyOf()
        scene.render(input(count = 12))
        assertTrue(!empty.contentEquals(scene.frame))
    }

    @Test
    fun `night is darker than day and the water follows the weather`() {
        val clear = OceanoPixelScene(OceanoWeather.Clear)
        clear.render(input(night = 0f))
        val day = clear.frame.meanLuma()
        clear.render(input(night = 1f))
        assertTrue(clear.frame.meanLuma() < day * 0.6, "night should be much darker")

        val storm = OceanoPixelScene(OceanoWeather.Storm)
        storm.render(input(night = 0f))
        assertTrue(storm.frame.meanLuma() < day, "a storm darkens the water")
    }

    @Test
    fun `each phase uses 24 scene colors and the glow colors only exist at night`() {
        assertEquals(Px.SCENE_COLORS + 1, OceanoPalette.day().size)
        assertEquals(Px.SCENE_COLORS + 1, OceanoPalette.night().size)
        assertEquals(0xFF3DFFE0.toInt(), OceanoPalette.night()[Px.GLOW_A])
        assertNotEquals(OceanoPalette.day()[Px.GLOW_A], OceanoPalette.night()[Px.GLOW_A])
    }

    @Test
    fun `every sprite only uses palette indices and the bell pulses`() {
        val all = buildList {
            OceanoSprites.BELLS.forEach { addAll(it) }
            OceanoSprites.TENTACLES.forEach { addAll(it) }
            addAll(OceanoSprites.TURTLE)
            addAll(OceanoSprites.ANTHIAS)
            addAll(OceanoSprites.ANGELFISH)
            add(OceanoSprites.SHARK)
        }
        for (s in all) assertTrue(s.px.all { it in 0..Px.JELLY_LIGHT }, "sprite index out of range")
        for (frames in OceanoSprites.BELLS) {
            assertTrue(frames.map { it.px.toList() }.distinct().size >= 3, "bell frames should differ")
            assertTrue(OceanoSprites.bellRows(OceanoSprites.BELLS.indexOf(frames), 2) < OceanoSprites.bellRows(OceanoSprites.BELLS.indexOf(frames), 0))
        }
    }

    @Test
    fun `the focused jelly is centred and the sizes step with the focus`() {
        assertEquals(160, OceanoLayout.jellyX(3, 3f))
        assertTrue(kotlin.math.abs(OceanoLayout.jellyX(4, 3f) - 160 - OceanoLayout.STEP) <= 1f)
        assertEquals(2, OceanoLayout.sizeIndex(OceanoLayout.focusOf(3, 3f)))
        assertEquals(1, OceanoLayout.sizeIndex(OceanoLayout.focusOf(3, 3.4f)))
        assertEquals(0, OceanoLayout.sizeIndex(OceanoLayout.focusOf(3, 5f)))
    }

    @Test
    fun `accented and odd first letters map to a glyph`() {
        assertEquals(OceanoSprites.glyphIndex('E'), OceanoSprites.glyphIndex('é'))
        assertEquals(0, OceanoSprites.glyphIndex('a'))
        assertEquals(26 + 7, OceanoSprites.glyphIndex('7'))
        assertEquals(OceanoSprites.glyphIndex('?'), OceanoSprites.glyphIndex('+'))
    }

    @Test
    fun `a banner icon shrinks to 16 by 9 posterised to four levels`() {
        val src = IntArray(320 * 180) { 0xFF336699.toInt() }
        val icon = OceanoIcon.fromArgb(src, 320, 180)
        assertEquals(16, icon.w)
        assertEquals(9, icon.h)
        assertTrue(icon.argb.all { ((it shr 16) and 255) in intArrayOf(0, 85, 170, 255) })
        assertEquals(9, OceanoIcon.fromArgb(IntArray(64 * 64), 64, 64).h)
    }
}
