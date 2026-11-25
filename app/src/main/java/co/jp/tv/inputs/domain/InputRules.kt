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
}
