package co.jp.tv.core.sound

/**
 * Decides whether a sound plays and how loud, so a held D-pad key ticks instead of machine-gunning.
 * Not thread-safe: call it from the thread that raises the events (the main thread).
 */
class SoundThrottle {
    private val last = mutableMapOf<UiSound, Long>()

    /** Gain 0..1 to play [sound] at [nowMs], or null when it should be skipped. */
    fun gainFor(sound: UiSound, nowMs: Long): Float? {
        val previous = last[sound]
        val gap = if (previous == null) Long.MAX_VALUE else nowMs - previous
        val repeating = sound == UiSound.Move || sound == UiSound.ControlFocus
        val minGap = if (repeating) MIN_MOVE_GAP_MS else MIN_ONE_SHOT_GAP_MS
        if (gap < minGap) return null
        last[sound] = nowMs
        return if (repeating && gap < REPEAT_WINDOW_MS) REPEAT_GAIN else 1f
    }

    companion object {
        const val MIN_MOVE_GAP_MS = 60L
        const val MIN_ONE_SHOT_GAP_MS = 40L

        /** Moves closer together than this count as a held key. */
        const val REPEAT_WINDOW_MS = 250L
        const val REPEAT_GAIN = 0.55f
    }
}
