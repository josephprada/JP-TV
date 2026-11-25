package co.jp.tv.inputs.domain

/** One external video input of the device (an HDMI port, composite jack...), as Android TV lists it. */
data class InputSource(
    val id: String,
    val kind: Kind,
    /** The name the firmware or the user gave it; null when there is none worth showing. */
    val label: String?,
    val signal: Signal,
) {
    enum class Kind { HDMI, COMPOSITE, COMPONENT, VGA, OTHER }

    enum class Signal { CONNECTED, STANDBY, DISCONNECTED, UNKNOWN }
}
