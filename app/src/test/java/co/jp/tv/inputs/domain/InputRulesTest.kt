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
}
