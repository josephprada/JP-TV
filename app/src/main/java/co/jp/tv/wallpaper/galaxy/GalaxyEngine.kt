package co.jp.tv.wallpaper.galaxy

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Star field and meteor simulation, ported from JP-Wallet's galaxyEngine.
 * Everything is precomputed into flat arrays; [update] and the draw code never allocate.
 *
 * @param density display density: star radii are in design px, so they are scaled to stay fine.
 */
internal class GalaxyEngine(
    val width: Float,
    val height: Float,
    density: Float,
    seed: Long = 7L,
) {
    private val random = Random(seed)

    // Design is 1920 wide; meteor speed and length scale with the actual width.
    private val designScale = width / 1920f

    val starCount: Int = ((width * height) / 9000f).toInt()
    val starX = FloatArray(starCount)
    val starY = FloatArray(starCount)
    val starRadius = FloatArray(starCount)
    val starBrightness = FloatArray(starCount)
    val starPhase = FloatArray(starCount)
    val starSpeed = FloatArray(starCount)
    val starIsAccent = BooleanArray(starCount)

    val meteorX = FloatArray(MAX_METEORS)
    val meteorY = FloatArray(MAX_METEORS)
    val meteorVx = FloatArray(MAX_METEORS)
    val meteorVy = FloatArray(MAX_METEORS)
    val meteorLength = FloatArray(MAX_METEORS)
    val meteorOpacity = FloatArray(MAX_METEORS)
    private val meteorLife = FloatArray(MAX_METEORS)
    val meteorAlive = BooleanArray(MAX_METEORS)

    var aliveMeteors = 0
        private set

    private var lastMs = -1L
    private var nextSpawnMs = 0L

    init {
        for (i in 0 until starCount) {
            starX[i] = random.nextFloat() * width
            starY[i] = random.nextFloat() * height
            val r = if (random.nextFloat() > 0.92f) 1.4f else random.nextFloat() * 0.9f + 0.3f
            starRadius[i] = r * density
            starBrightness[i] = 0.35f + random.nextFloat() * 0.65f
            starPhase[i] = random.nextFloat() * TWO_PI
            starSpeed[i] = 0.4f + random.nextFloat()
            starIsAccent[i] = r > 1.1f && starBrightness[i] > 0.75f
        }
    }

    /** Twinkle alpha of star [i] at time [tMs]; never negative. */
    fun twinkle(i: Int, tMs: Long): Float {
        val wave = sin(tMs * 0.0012 * starSpeed[i] + starPhase[i]).toFloat()
        return (starBrightness[i] * (0.45f + 0.55f * wave)).coerceAtLeast(0f)
    }

    /** Advances meteors to [nowMs]; spawns at most one meteor per call. */
    fun update(nowMs: Long) {
        val dt = if (lastMs < 0) 0L else (nowMs - lastMs).coerceIn(0L, 100L)
        lastMs = nowMs
        val k = dt / FRAME_MS

        if (nowMs >= nextSpawnMs) {
            nextSpawnMs = nowMs + random.nextInt(1200, 3800)
            if (aliveMeteors < MAX_METEORS) spawn()
        }

        if (k <= 0f) return
        val fade = FADE_PER_FRAME.pow(k)
        for (i in 0 until MAX_METEORS) {
            if (!meteorAlive[i]) continue
            meteorX[i] += meteorVx[i] * k
            meteorY[i] += meteorVy[i] * k
            meteorLife[i] -= 0.008f * k
            meteorOpacity[i] *= fade
            val outOfBounds = meteorX[i] > width + 200f * designScale + meteorLength[i] ||
                meteorY[i] < -200f * designScale - meteorLength[i]
            if (outOfBounds || meteorLife[i] <= 0f || meteorOpacity[i] < 0.02f) {
                meteorAlive[i] = false
                aliveMeteors--
            }
        }
    }

    private fun spawn() {
        val slot = (0 until MAX_METEORS).first { !meteorAlive[it] }
        val angle = -0.95f + random.nextFloat() * 0.4f
        val speed = (7f + random.nextFloat() * 7f) * designScale
        meteorVx[slot] = cos(angle) * speed
        meteorVy[slot] = sin(angle) * speed
        if (random.nextFloat() < 0.65f) {
            meteorX[slot] = -120f * designScale + random.nextFloat() * (width + 240f * designScale)
            meteorY[slot] = -120f * designScale + random.nextFloat() * (0.45f * height + 120f * designScale)
        } else {
            meteorX[slot] = -120f * designScale + random.nextFloat() * (0.55f * width + 120f * designScale)
            meteorY[slot] = -120f * designScale + random.nextFloat() * (0.35f * height + 120f * designScale)
        }
        meteorLength[slot] = (90f + random.nextFloat() * 90f) * designScale
        meteorOpacity[slot] = 0.55f + random.nextFloat() * 0.4f
        meteorLife[slot] = 1f
        meteorAlive[slot] = true
        aliveMeteors++
    }

    companion object {
        const val MAX_METEORS = 4
        private const val FRAME_MS = 1000f / 60f
        private const val FADE_PER_FRAME = 0.992f
        private const val TWO_PI = (2.0 * Math.PI).toFloat()
    }
}
