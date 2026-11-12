package co.jp.tv.weather.domain

/** A town the weather is read for. */
data class WeatherPlace(
    val name: String,
    /** Department or state, when known. */
    val admin1: String?,
    val lat: Double,
    val lon: Double,
) {
    /** "Girón, Santander", or just the name when there is no department. */
    val label: String get() = if (admin1.isNullOrBlank()) name else "$name, $admin1"

    companion object {
        val Default = WeatherPlace("Bucaramanga", "Santander", 7.1254, -73.1198)

        /**
         * Metropolitan area and the main towns of Santander. Coordinates are the town centres to about
         * 0.01 degrees, which is finer than the ~11 km grid the forecast model works on.
         */
        val QuickPicks: List<WeatherPlace> = listOf(
            Default,
            WeatherPlace("Floridablanca", "Santander", 7.0622, -73.0864),
            WeatherPlace("Girón", "Santander", 7.0683, -73.1697),
            WeatherPlace("Piedecuesta", "Santander", 6.9876, -73.0505),
            WeatherPlace("Lebrija", "Santander", 7.1181, -73.2178),
            WeatherPlace("Los Santos", "Santander", 6.7558, -73.1058),
            WeatherPlace("San Gil", "Santander", 6.5553, -73.1347),
            WeatherPlace("Barrancabermeja", "Santander", 7.0653, -73.8547),
            WeatherPlace("Socorro", "Santander", 6.4667, -73.2600),
            WeatherPlace("Barichara", "Santander", 6.6361, -73.2231),
        )
    }
}

/** One-line text form of a [WeatherPlace] for DataStore. Anything unreadable decodes to the default town. */
object WeatherPlaceCodec {
    private const val SEPARATOR = '\t'
    private const val FIELDS = 4

    fun encode(place: WeatherPlace): String =
        listOf(clean(place.name), clean(place.admin1.orEmpty()), place.lat.toString(), place.lon.toString())
            .joinToString(SEPARATOR.toString())

    fun decode(raw: String?): WeatherPlace {
        val parts = raw?.split(SEPARATOR) ?: return WeatherPlace.Default
        if (parts.size != FIELDS) return WeatherPlace.Default
        val name = parts[0].trim()
        val lat = parts[2].toDoubleOrNull()
        val lon = parts[3].toDoubleOrNull()
        if (name.isEmpty() || lat == null || lon == null) return WeatherPlace.Default
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return WeatherPlace.Default
        return WeatherPlace(name, parts[1].trim().ifEmpty { null }, lat, lon)
    }

    private fun clean(text: String) = text.map { if (it.isISOControl()) ' ' else it }.joinToString("").trim()
}
