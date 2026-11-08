package co.jp.tv.preferences.domain

/** Title/clock fonts offered on the Customize screen (all OFL, bundled under res/font). */
data class TitleFont(val id: String, val displayName: String)

object TitleFonts {
    const val DEFAULT_ID = "anton"

    val all: List<TitleFont> = listOf(
        TitleFont("anton", "Anton"),
        TitleFont("bebas_neue", "Bebas Neue"),
        TitleFont("archivo_black", "Archivo Black"),
        TitleFont("space_grotesk", "Space Grotesk"),
        TitleFont("instrument_serif", "Instrument Serif"),
    )

    fun byId(id: String): TitleFont = all.firstOrNull { it.id == id } ?: all.first()
}
