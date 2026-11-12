package co.jp.tv.homeoverride.domain

/** What the settings row shows. */
enum class HomeOverrideStatus {
    ACTIVE,
    DISABLED,
    NEEDS_PERMISSION,
}

/** Reads the value of Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES (components joined by ':'). */
object AccessibilityServices {
    fun isEnabled(rawList: String?, packageName: String, className: String): Boolean =
        rawList.orEmpty().split(':').map { it.trim() }.filter { it.isNotEmpty() }.any { entry ->
            val pkg = entry.substringBefore('/', missingDelimiterValue = "")
            val cls = entry.substringAfter('/', missingDelimiterValue = "")
            val full = if (cls.startsWith('.')) pkg + cls else cls
            pkg == packageName && full == className
        }
}
