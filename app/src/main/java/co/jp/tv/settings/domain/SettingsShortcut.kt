package co.jp.tv.settings.domain

/** One tile of the quick settings screen. */
data class SettingsShortcut(val id: Id, val target: ShortcutTarget) {
    enum class Id { NETWORK, BLUETOOTH, DISPLAY, SOUND, APPS, STORAGE, DATE_TIME, THEME }
}

sealed interface ShortcutTarget {
    /** Opens the launcher's own network panel. */
    data object NetworkPanel : ShortcutTarget

    /** Opens a native Android settings screen; [fallbackAction] is used when [action] has no handler. */
    data class Native(val action: String, val fallbackAction: String) : ShortcutTarget

    /** Not available yet (phase 5). */
    data object Disabled : ShortcutTarget
}
