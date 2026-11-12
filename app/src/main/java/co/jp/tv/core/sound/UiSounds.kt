package co.jp.tv.core.sound

import androidx.compose.runtime.staticCompositionLocalOf

/** The four interface sounds every theme provides in its own voice. */
enum class UiSound { Move, Select, Back, ControlFocus }

/** User-facing loudness of the interface sounds. */
enum class SoundVolume(val gain: Float) {
    LOW(0.25f),
    MEDIUM(0.5f),
    HIGH(0.85f),
    ;

    /** One step along low, medium, high (stops at the ends). */
    fun step(direction: Int): SoundVolume = entries[(ordinal + direction).coerceIn(0, entries.lastIndex)]
}

/**
 * Port: short interface sounds. Implementations must never block the caller; [play] is fire-and-forget
 * and silently does nothing while disabled, stopped or before the set has loaded.
 */
interface UiSounds {
    fun play(sound: UiSound)

    /** Makes [setId] the active sound palette, loading it and unloading the previous one. */
    fun useSet(setId: String)

    fun configure(enabled: Boolean, volume: SoundVolume)

    /** False while the launcher is stopped: nothing plays. */
    fun setActive(active: Boolean)

    object None : UiSounds {
        override fun play(sound: UiSound) = Unit
        override fun useSet(setId: String) = Unit
        override fun configure(enabled: Boolean, volume: SoundVolume) = Unit
        override fun setActive(active: Boolean) = Unit
    }
}

/** Sounds for the current composition; [UiSounds.None] in previews and tests. */
val LocalUiSounds = staticCompositionLocalOf<UiSounds> { UiSounds.None }
