package co.jp.tv.themes.shared

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class RailWindowTest {
    @Test
    fun `focus inside window keeps top`() {
        assertEquals(2, RailWindow.top(previousTop = 2, focus = 4, count = 20, size = 7))
    }

    @Test
    fun `focus above window pulls top up`() {
        assertEquals(1, RailWindow.top(previousTop = 3, focus = 1, count = 20, size = 7))
    }

    @Test
    fun `focus below window pushes top down so focus is the last row`() {
        assertEquals(3, RailWindow.top(previousTop = 0, focus = 9, count = 20, size = 7))
    }

    @Test
    fun `short lists never scroll`() {
        assertEquals(0, RailWindow.top(previousTop = 3, focus = 2, count = 5, size = 7))
    }

    @Test
    fun `top never leaves the list tail empty`() {
        assertEquals(3, RailWindow.top(previousTop = 8, focus = 9, count = 10, size = 7))
    }

    @Test
    fun `counts hidden rows above and below`() {
        assertEquals(3, RailWindow.above(top = 3))
        assertEquals(10, RailWindow.below(top = 3, count = 20, size = 7))
        assertEquals(0, RailWindow.below(top = 3, count = 10, size = 7))
    }
}
