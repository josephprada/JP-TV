package co.jp.tv.inputs.domain

import co.jp.tv.inputs.domain.InputSource.Kind
import co.jp.tv.inputs.domain.InputSource.Signal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InputRulesTest {
    private fun source(id: String, kind: Kind, label: String? = null) =
        InputSource(id, kind, label, Signal.UNKNOWN)

    @Test
    fun `android tv input types map to kinds`() {
        assertEquals(Kind.HDMI, InputRules.kindOfType(1007))
        assertEquals(Kind.COMPOSITE, InputRules.kindOfType(1001))
        assertEquals(Kind.COMPONENT, InputRules.kindOfType(1004))
        assertEquals(Kind.VGA, InputRules.kindOfType(1005))
        assertEquals(Kind.OTHER, InputRules.kindOfType(1000))
        assertEquals(Kind.OTHER, InputRules.kindOfType(4242))
    }

    @Test
    fun `input states map to signals`() {
        assertEquals(Signal.CONNECTED, InputRules.signalOf(2))
        assertEquals(Signal.STANDBY, InputRules.signalOf(1))
        assertEquals(Signal.DISCONNECTED, InputRules.signalOf(0))
        assertEquals(Signal.UNKNOWN, InputRules.signalOf(99))
    }

    @Test
    fun `hdmi comes first ordered by label, then the other kinds, with ids breaking ties`() {
        val sorted = InputRules.sorted(
            listOf(
                source("vga", Kind.VGA),
                source("b", Kind.HDMI, label = "HDMI 3"),
                source("av", Kind.COMPOSITE),
                source("a", Kind.HDMI, label = "HDMI 1"),
                source("z2", Kind.HDMI),
                source("z1", Kind.HDMI),
                source("c", Kind.HDMI, label = "HDMI 2"),
            ),
        )
        assertEquals(listOf("z1", "z2", "a", "c", "b", "av", "vga"), sorted.map { it.id })
    }

    @Test
    fun `a blank custom label counts as no label`() {
        assertEquals(null, InputRules.ownLabel("   "))
        assertEquals(null, InputRules.ownLabel(null))
        assertEquals("Consola", InputRules.ownLabel("  Consola "))
    }

    private fun signal(id: String, signal: Signal, kind: Kind = Kind.HDMI) = InputSource(id, kind, null, signal)

    @Test
    fun `hidden inputs are split from the visible ones keeping the order`() {
        val all = listOf(source("a", Kind.HDMI), source("b", Kind.HDMI), source("c", Kind.COMPOSITE))
        val (shown, hidden) = InputRules.partition(all, setOf("b"))
        assertEquals(listOf("a", "c"), shown.map { it.id })
        assertEquals(listOf("b"), hidden.map { it.id })
    }

    @Test
    fun `nothing hidden keeps every input visible and ignores ids the device no longer lists`() {
        val all = listOf(source("a", Kind.HDMI))
        val (shown, hidden) = InputRules.partition(all, setOf("gone"))
        assertEquals(listOf("a"), shown.map { it.id })
        assertEquals(emptyList<InputSource>(), hidden)
    }

    @Test
    fun `a real signal brings a hidden input back`() {
        val sources = listOf(signal("hdmi1", Signal.CONNECTED), signal("hdmi2", Signal.STANDBY), signal("av1", Signal.UNKNOWN))
        assertEquals(setOf("hdmi2"), InputRules.revealConnected(setOf("hdmi1", "hdmi2"), sources))
    }

    @Test
    fun `standby disconnected and unknown never unhide anything`() {
        val sources = listOf(signal("a", Signal.STANDBY), signal("b", Signal.DISCONNECTED), signal("c", Signal.UNKNOWN))
        val hidden = setOf("a", "b", "c")
        assertEquals(hidden, InputRules.revealConnected(hidden, sources))
    }

    @Test
    fun `revealing keeps ids of inputs that are not listed right now`() {
        assertEquals(setOf("offline"), InputRules.revealConnected(setOf("offline", "a"), listOf(signal("a", Signal.CONNECTED))))
    }

    @Test
    fun `state change callbacks unhide only on the connected state`() {
        assertEquals(setOf("a"), InputRules.revealOnState(setOf("a"), "a", 1))
        assertEquals(setOf("a"), InputRules.revealOnState(setOf("a"), "a", 0))
        assertEquals(emptySet<String>(), InputRules.revealOnState(setOf("a"), "a", 2))
        assertEquals(setOf("a"), InputRules.revealOnState(setOf("a"), "other", 2))
    }
}
