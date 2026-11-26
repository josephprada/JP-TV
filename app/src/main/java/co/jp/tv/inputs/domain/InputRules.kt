package co.jp.tv.inputs.domain

import co.jp.tv.inputs.domain.InputSource.Kind
import co.jp.tv.inputs.domain.InputSource.Signal

/** Pure rules for the inputs panel; the numbers are the `TvInputInfo` / `TvInputManager` constants. */
object InputRules {
    fun kindOfType(type: Int): Kind = when (type) {
        1007 -> Kind.HDMI // TYPE_HDMI
        1001 -> Kind.COMPOSITE // TYPE_COMPOSITE
        1004 -> Kind.COMPONENT // TYPE_COMPONENT
        1005 -> Kind.VGA // TYPE_VGA
        else -> Kind.OTHER
    }

    fun signalOf(state: Int): Signal = when (state) {
        2 -> Signal.CONNECTED // INPUT_STATE_CONNECTED
        1 -> Signal.STANDBY // INPUT_STATE_CONNECTED_STANDBY
        0 -> Signal.DISCONNECTED // INPUT_STATE_DISCONNECTED
        else -> Signal.UNKNOWN
    }

    /** HDMI first, then the other kinds; inside a kind by label, then id. The order never depends on the signal. */
    fun sorted(sources: List<InputSource>): List<InputSource> =
        sources.sortedWith(
            compareBy<InputSource> { it.kind.ordinal }
                .thenBy { it.label ?: "" }
                .thenBy { it.id },
        )

    /** The label to show as is, or null so the UI falls back to a generic name like "HDMI 2". */
    fun ownLabel(raw: CharSequence?): String? = raw?.toString()?.trim()?.takeIf { it.isNotEmpty() }

    /** Splits [sources] into the ones to list and the ones the user hid, both in their original order. */
    fun partition(sources: List<InputSource>, hidden: Set<String>): Pair<List<InputSource>, List<InputSource>> =
        sources.partition { it.id !in hidden }

    /**
     * A real signal proves the port exists, so any hidden input that reports it leaves the hidden set.
     * Standby and unknown states say nothing: the HY320 firmware reports its phantom ports as standby.
     */
    fun revealConnected(hidden: Set<String>, sources: List<InputSource>): Set<String> {
        val live = sources.filter { it.signal == Signal.CONNECTED }.mapTo(HashSet()) { it.id }
        return hidden - live
    }

    /** Same rule for a single `onInputStateChanged` callback (the raw `INPUT_STATE_*` value). */
    fun revealOnState(hidden: Set<String>, inputId: String, state: Int): Set<String> =
        if (signalOf(state) == Signal.CONNECTED) hidden - inputId else hidden
}
