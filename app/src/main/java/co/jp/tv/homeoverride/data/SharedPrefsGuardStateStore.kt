package co.jp.tv.homeoverride.data

import android.content.Context
import co.jp.tv.homeoverride.domain.GuardState
import co.jp.tv.homeoverride.domain.GuardStateStore

/** Adapter: the guard memory in SharedPreferences, written synchronously so a crash cannot lose it. */
class SharedPrefsGuardStateStore(context: Context) : GuardStateStore {
    private val prefs = context.applicationContext.getSharedPreferences("home_override_guard", Context.MODE_PRIVATE)

    override fun load() = GuardState(
        troubleAt = prefs.getString(TROUBLES, "").orEmpty().split(',').mapNotNull { it.toLongOrNull() },
        graceUntil = prefs.getLong(GRACE_UNTIL, 0L),
        pendingSince = prefs.getLong(PENDING, NONE).takeIf { it != NONE },
    )

    override fun save(state: GuardState) {
        prefs.edit()
            .putString(TROUBLES, state.troubleAt.joinToString(","))
            .putLong(GRACE_UNTIL, state.graceUntil)
            .putLong(PENDING, state.pendingSince ?: NONE)
            .commit()
    }

    private companion object {
        const val TROUBLES = "trouble_at"
        const val GRACE_UNTIL = "grace_until"
        const val PENDING = "pending_since"
        const val NONE = -1L
    }
}
