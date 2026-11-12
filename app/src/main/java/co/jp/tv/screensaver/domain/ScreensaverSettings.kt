package co.jp.tv.screensaver.domain

/** What the screensaver shows. [RANDOM] rotates through the other four every ten minutes. */
enum class ScreensaverMode {
    PHOTOS,
    CLOCK,
    LIVE,
    AMBIENT,
    RANDOM,
    ;

    companion object {
        val Default = CLOCK

        fun fromName(name: String?): ScreensaverMode = entries.firstOrNull { it.name == name } ?: Default
    }
}

/** The screensaver choices persisted in the launcher config. */
data class ScreensaverSettings(
    val mode: ScreensaverMode = ScreensaverMode.Default,
    /** Minutes without a key press before the in-app saver starts; 0 is never. */
    val idleMinutes: Int = IdleRules.DEFAULT_MINUTES,
    /** The line the clock mode shows under the time. */
    val message: String = DEFAULT_MESSAGE,
) {
    companion object {
        const val DEFAULT_MESSAGE = "JP-TV"
        const val MAX_MESSAGE = 40

        val Default = ScreensaverSettings()

        /** What the message field may save: trimmed, at most [MAX_MESSAGE] characters, never empty. */
        fun sanitizeMessage(raw: String): String = raw.trim().take(MAX_MESSAGE).trim().ifEmpty { DEFAULT_MESSAGE }
    }
}
