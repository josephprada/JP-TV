package co.jp.tv.preferences.domain

/** The six accent swatches from Customize.dc.html. */
data class AccentPreset(val id: String, val displayName: String, val argb: Long) {
    val hex: String
        get() = "#%06X".format(argb and 0xFFFFFF)
}

object AccentPresets {
    val all: List<AccentPreset> = listOf(
        AccentPreset("rojo", "Rojo JP", 0xFFE63329),
        AccentPreset("naranja", "Naranja", 0xFFFF8A1F),
        AccentPreset("oro", "Oro", 0xFFF2C14E),
        AccentPreset("verde", "Verde", 0xFF2FBF71),
        AccentPreset("azul", "Azul", 0xFF3D8BFD),
        AccentPreset("violeta", "Violeta", 0xFFB26BFF),
    )

    val DEFAULT: AccentPreset = all.first()

    fun byArgb(argb: Long): AccentPreset =
        all.firstOrNull { it.argb == argb } ?: DEFAULT
}
