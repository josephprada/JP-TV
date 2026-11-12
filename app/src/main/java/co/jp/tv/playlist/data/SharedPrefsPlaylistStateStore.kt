package co.jp.tv.playlist.data

import android.content.Context
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistPosition
import co.jp.tv.playlist.domain.PlaylistStateStore

/** Adapter: each playlist's last slot and the time it got there, so a restarted process resumes instead of starting over. */
class SharedPrefsPlaylistStateStore(context: Context) : PlaylistStateStore {
    private val prefs = context.applicationContext.getSharedPreferences("playlist_state", Context.MODE_PRIVATE)

    override fun load(kind: MediaKind): PlaylistPosition? {
        val changedAt = prefs.getLong("${kind.name}_changed_at", NONE)
        if (changedAt == NONE) return null
        return PlaylistPosition(prefs.getInt("${kind.name}_index", 0), changedAt)
    }

    override fun save(kind: MediaKind, position: PlaylistPosition) {
        prefs.edit()
            .putInt("${kind.name}_index", position.index)
            .putLong("${kind.name}_changed_at", position.changedAtMs)
            .apply()
    }

    private companion object {
        const val NONE = -1L
    }
}
