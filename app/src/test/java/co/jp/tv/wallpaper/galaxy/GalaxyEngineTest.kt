package co.jp.tv.wallpaper.galaxy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GalaxyEngineTest {
    @Test
    fun `star count is one per 9000 square pixels`() {
        assertEquals(102, GalaxyEngine(1280f, 720f, 1f).starCount)
        assertEquals(230, GalaxyEngine(1920f, 1080f, 1f).starCount)
    }

    @Test
    fun `low-end star budget halves the field`() {
        assertEquals(51, GalaxyEngine(1280f, 720f, 1f, starBudget = 0.5f).starCount)
    }

    @Test
    fun `star properties stay inside the documented ranges`() {
        val e = GalaxyEngine(1280f, 720f, density = 1f)
        for (i in 0 until e.starCount) {
            assertTrue(e.starBrightness[i] in 0.35f..1f)
            assertTrue(e.starSpeed[i] in 0.4f..1.4f)
            assertTrue(e.starRadius[i] in 0.3f..1.4f)
            assertTrue(e.twinkle(i, tMs = i * 977L) >= 0f)
        }
    }

    @Test
    fun `radii scale with density`() {
        val a = GalaxyEngine(1280f, 720f, density = 1f)
        val b = GalaxyEngine(1280f, 720f, density = 2f)
        assertEquals(a.starRadius[0] * 2f, b.starRadius[0])
    }

    @Test
    fun `meteors never exceed four and fly up and to the right`() {
        val e = GalaxyEngine(1280f, 720f, density = 1f)
        var now = 0L
        var maxAlive = 0
        repeat(2000) {
            e.update(now)
            now += 16
            maxAlive = maxOf(maxAlive, e.aliveMeteors)
            for (i in 0 until GalaxyEngine.MAX_METEORS) {
                if (e.meteorAlive[i]) {
                    assertTrue(e.meteorVx[i] > 0f && e.meteorVy[i] < 0f)
                }
            }
        }
        assertTrue(maxAlive in 1..GalaxyEngine.MAX_METEORS)
    }

    @Test
    fun `meteors eventually leave or fade so slots are reused`() {
        val e = GalaxyEngine(1280f, 720f, density = 1f)
        var now = 0L
        var spawnedAfterWarmup = false
        repeat(3000) {
            val before = e.aliveMeteors
            e.update(now)
            now += 16
            if (now > 30_000 && e.aliveMeteors > before) spawnedAfterWarmup = true
        }
        assertTrue(spawnedAfterWarmup)
    }
}
