package co.jp.tv.playlist.domain

/** How often a playlist moves on to its next item. [minutes] is null for the modes that do not follow the clock. */
enum class RotationInterval(val minutes: Int?) {
    NEVER(null),
    MIN_5(5),
    MIN_15(15),
    MIN_30(30),
    MIN_60(60),
    ON_HOME_RETURN(null),
    ;

    companion object {
        val Default = MIN_15

        fun fromName(name: String?): RotationInterval = entries.firstOrNull { it.name == name } ?: Default
    }
}

enum class PlaylistOrder {
    SEQUENTIAL,
    SHUFFLE,
    ;

    companion object {
        fun fromName(name: String?): PlaylistOrder = entries.firstOrNull { it.name == name } ?: SEQUENTIAL
    }
}

/**
 * One playlist's choices. [selected] holds file ids; empty means "every file found", so new files join the
 * list by themselves and the legacy single file keeps working with no migration.
 */
data class PlaylistSettings(
    val selected: List<String> = emptyList(),
    val interval: RotationInterval = RotationInterval.Default,
    val order: PlaylistOrder = PlaylistOrder.SEQUENTIAL,
) {
    fun isSelected(found: List<MediaItem>, id: String): Boolean =
        selected.isEmpty() || (id in selected && found.any { it.id == id })

    /** Switches [id] on or off. The last file can't be switched off; selecting every file goes back to "all". */
    fun toggle(found: List<MediaItem>, id: String): PlaylistSettings {
        val current = PlaylistResolver.resolve(found, this).map { it.id }
        val next = if (id in current) current - id else found.map { it.id }.filter { it == id || it in current }
        if (next.isEmpty()) return this
        val everything = found.map { it.id }
        return copy(selected = if (next.toSet() == everything.toSet()) emptyList() else everything.filter { it in next })
    }

    companion object {
        val Default = PlaylistSettings()
    }
}

/** What a playlist plays right now: the resolved files plus how to move through them. */
data class PlaylistSelection(
    val items: List<MediaItem>,
    val interval: RotationInterval = RotationInterval.Default,
    val order: PlaylistOrder = PlaylistOrder.SEQUENTIAL,
) {
    companion object {
        val Empty = PlaylistSelection(emptyList())

        fun of(found: List<MediaItem>, settings: PlaylistSettings) =
            PlaylistSelection(PlaylistResolver.resolve(found, settings), settings.interval, settings.order)
    }
}

object PlaylistResolver {
    /** The files to play: the chosen ones that still exist (in the order found), or every file when none applies. */
    fun resolve(found: List<MediaItem>, settings: PlaylistSettings): List<MediaItem> {
        if (settings.selected.isEmpty()) return found
        val chosen = found.filter { it.id in settings.selected }
        return chosen.ifEmpty { found }
    }
}
