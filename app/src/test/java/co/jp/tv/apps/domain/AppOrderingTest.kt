package co.jp.tv.apps.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppOrderingTest {
    private fun ordering(order: String, hidden: String = "") = AppOrdering(
        order = order.split(" ").filter { it.isNotEmpty() },
        hidden = hidden.split(" ").filter { it.isNotEmpty() }.toSet(),
    )

    @Test
    fun `reconcile keeps saved order first and appends new apps in default order`() {
        val saved = ordering("c a")
        val result = saved.reconcile(defaultOrder = listOf("a", "b", "c", "d"))
        assertEquals(listOf("c", "a", "b", "d"), result.order)
    }

    @Test
    fun `reconcile silently drops removed packages from order and hidden`() {
        val saved = ordering("x a y b", hidden = "x b")
        val result = saved.reconcile(defaultOrder = listOf("a", "b"))
        assertEquals(listOf("a", "b"), result.order)
        assertEquals(setOf("b"), result.hidden)
    }

    @Test
    fun `reconcile removes duplicates`() {
        val result = ordering("a a b").reconcile(listOf("a", "b"))
        assertEquals(listOf("a", "b"), result.order)
    }

    @Test
    fun `visible excludes hidden and hiddenOrdered keeps their order`() {
        val o = ordering("a b c d", hidden = "d b")
        assertEquals(listOf("a", "c"), o.visible)
        assertEquals(listOf("b", "d"), o.hiddenOrdered)
    }

    @Test
    fun `moveUp swaps with the previous visible app`() {
        assertEquals(listOf("b", "a", "c"), ordering("a b c").moveUp("b").order)
    }

    @Test
    fun `moveUp skips hidden neighbours`() {
        val result = ordering("a h b", hidden = "h").moveUp("b")
        assertEquals(listOf("a"), result.visible.drop(1))
        assertEquals(listOf("b", "a"), result.visible)
    }

    @Test
    fun `moveUp on the first app is a no-op`() {
        val o = ordering("a b")
        assertEquals(o, o.moveUp("a"))
    }

    @Test
    fun `moveDown swaps with the next visible app`() {
        assertEquals(listOf("a", "c", "b"), ordering("a b c").moveDown("b").order)
    }

    @Test
    fun `moveDown on the last app is a no-op`() {
        val o = ordering("a b")
        assertEquals(o, o.moveDown("b"))
    }

    @Test
    fun `moveTo places the app at the visible index`() {
        assertEquals(listOf("c", "a", "b", "d"), ordering("a b c d").moveTo("c", 0).visible)
        assertEquals(listOf("b", "c", "d", "a"), ordering("a b c d").moveTo("a", 3).visible)
        assertEquals(listOf("a", "c", "b", "d"), ordering("a b c d").moveTo("b", 2).visible)
    }

    @Test
    fun `moveTo clamps the index`() {
        assertEquals(listOf("b", "c", "a"), ordering("a b c").moveTo("a", 99).visible)
        assertEquals(listOf("c", "a", "b"), ordering("a b c").moveTo("c", -4).visible)
    }

    @Test
    fun `moves ignore unknown and hidden packages`() {
        val o = ordering("a b", hidden = "b")
        assertEquals(o, o.moveUp("zzz"))
        assertEquals(o, o.moveTo("b", 0))
    }

    @Test
    fun `hide removes the app from visible and keeps its slot`() {
        val result = ordering("a b c").hide("b")
        assertEquals(listOf("a", "c"), result.visible)
        assertEquals(listOf("a", "b", "c"), result.order)
    }

    @Test
    fun `show brings the app back to its previous slot`() {
        val result = ordering("a b c").hide("b").show("b")
        assertEquals(listOf("a", "b", "c"), result.visible)
    }

    @Test
    fun `hide and show ignore unknown packages`() {
        val o = ordering("a")
        assertEquals(o, o.hide("zzz"))
        assertEquals(o, o.show("zzz"))
    }
}
