package co.jp.tv.core.sound.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import co.jp.tv.core.sound.SoundThrottle
import co.jp.tv.core.sound.SoundVolume
import co.jp.tv.core.sound.UiSound
import co.jp.tv.core.sound.UiSounds
import java.util.EnumMap
import java.util.concurrent.ConcurrentHashMap

/**
 * Adapter: [SoundPool] playing the short `snd_<set>_<sound>` clips from `res/raw`.
 *
 * Only the active set is in memory: [useSet] loads the new clips (SoundPool decodes on its own thread, so the
 * caller never waits) and unloads the previous ones. [play] is fire-and-forget, throttled by [SoundThrottle].
 * Call everything from the main thread.
 */
class SoundPoolUiSounds(
    context: Context,
    private val uptimeMs: () -> Long = SystemClock::uptimeMillis,
) : UiSounds {
    private val appContext = context.applicationContext
    private val pool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val throttle = SoundThrottle()

    // Sample ids of the active set; a clip plays only once the pool reports it decoded.
    private var activeSet: String? = null
    private var samples = EnumMap<UiSound, Int>(UiSound::class.java)
    private val decoded = ConcurrentHashMap.newKeySet<Int>()

    private var enabled = true
    private var active = true
    private var volume = SoundVolume.MEDIUM

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) decoded += sampleId }
    }

    override fun useSet(setId: String) {
        if (setId == activeSet) return
        samples.values.forEach { id ->
            decoded -= id
            pool.unload(id)
        }
        activeSet = setId
        samples = EnumMap(UiSound::class.java)
        UiSound.entries.forEach { sound ->
            val res = appContext.resources.getIdentifier(resourceName(setId, sound), "raw", appContext.packageName)
            if (res != 0) samples[sound] = pool.load(appContext, res, 1)
        }
    }

    override fun configure(enabled: Boolean, volume: SoundVolume) {
        this.enabled = enabled
        this.volume = volume
    }

    override fun setActive(active: Boolean) {
        this.active = active
        if (!active) pool.autoPause() else pool.autoResume()
    }

    override fun play(sound: UiSound) {
        if (!enabled || !active) return
        val id = samples[sound] ?: return
        if (id !in decoded) return
        val gain = throttle.gainFor(sound, uptimeMs()) ?: return
        val level = (gain * volume.gain).coerceIn(0f, 1f)
        pool.play(id, level, level, 1, 0, 1f)
    }

    /** Frees the pool; the instance is unusable afterwards. */
    fun release() = pool.release()

    companion object {
        const val MAX_STREAMS = 4

        /** `snd_zen_move`, `snd_zen_select`, `snd_zen_back`, `snd_zen_control`. */
        fun resourceName(setId: String, sound: UiSound): String {
            val key = when (sound) {
                UiSound.Move -> "move"
                UiSound.Select -> "select"
                UiSound.Back -> "back"
                UiSound.ControlFocus -> "control"
            }
            return "snd_${setId}_$key"
        }
    }
}
