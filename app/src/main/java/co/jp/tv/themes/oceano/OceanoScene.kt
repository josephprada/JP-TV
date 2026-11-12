package co.jp.tv.themes.oceano

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.themes.shared.rememberAppIcon
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The pixel scene on screen: one drawImage per frame of a 320x180 bitmap scaled with nearest-neighbour,
 * so every virtual pixel stays a crisp square. The bitmap is only rewritten when something that shows
 * changed (a new animation step, a scroll step, the day/night fade), so between steps a redraw is just
 * the one image. [night] and [scroll] are read in the draw block, not during composition.
 */
@Composable
internal fun OceanoPixelView(
    clock: OceanoClock,
    night: () -> Float,
    scroll: () -> Float,
    focusedIndex: Int,
    apps: List<TvApp>,
    modifier: Modifier = Modifier,
    weather: OceanoWeather = OceanoWeather.Clear,
) {
    val scene = remember(weather) { OceanoPixelScene(weather) }
    val frames = remember { OceanoFrames() }
    val input = remember { OceanoSceneInput() }
    val initials = remember(apps) { IntArray(apps.size) { OceanoSprites.glyphIndex(apps[it].label.firstOrNull() ?: '?') } }
    val icon = rememberPixelIcon(apps.getOrNull(focusedIndex))
    Canvas(modifier.fillMaxSize()) {
        input.tick = clock.tick
        input.night = night()
        input.scroll = scroll()
        input.focused = focusedIndex
        input.count = apps.size
        input.initials = initials
        input.icon = icon
        frames.draw(this, scene, input)
    }
}

/** Two bitmaps taken in turn, so the render thread never reads the one being rewritten. */
private class OceanoFrames {
    private val bitmaps = Array(2) { Bitmap.createBitmap(OceanoPixels.W, OceanoPixels.H, Bitmap.Config.ARGB_8888).also { it.setHasAlpha(false) } }
    private val images: Array<ImageBitmap> = Array(2) { bitmaps[it].asImageBitmap() }
    private var shown = -1
    private var lastScene: OceanoPixelScene? = null
    private var lastTick = -1
    private var lastNight = -1
    private var lastScroll = Int.MIN_VALUE
    private var lastFocused = -1
    private var lastCount = -1
    private var lastInitials: IntArray? = null
    private var lastIcon: OceanoIcon? = null

    fun draw(scope: DrawScope, scene: OceanoPixelScene, input: OceanoSceneInput) {
        val night = (input.night * 16f).roundToInt()
        val scroll = (input.scroll * 48f).roundToInt()
        val dirty = shown < 0 || scene !== lastScene || input.tick != lastTick || night != lastNight ||
            scroll != lastScroll || input.focused != lastFocused || input.count != lastCount ||
            input.initials !== lastInitials || input.icon !== lastIcon
        if (dirty) {
            lastScene = scene
            lastTick = input.tick
            lastNight = night
            lastScroll = scroll
            lastFocused = input.focused
            lastCount = input.count
            lastInitials = input.initials
            lastIcon = input.icon
            scene.render(input)
            val next = if (shown == 0) 1 else 0
            bitmaps[next].setPixels(scene.frame, 0, OceanoPixels.W, 0, 0, OceanoPixels.W, OceanoPixels.H)
            shown = next
        }
        with(scope) {
            drawImage(
                images[shown],
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(OceanoPixels.W, OceanoPixels.H),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                filterQuality = FilterQuality.None,
            )
        }
    }
}

/** The focused app's icon cut to the pixel grid, off the main thread; null until it is ready or when there is none. */
@Composable
internal fun rememberPixelIcon(app: TvApp?): OceanoIcon? {
    if (app == null) return null
    // Keyed by app so a stale bitmap of the previous app is never shown on the new jelly.
    return key(app.packageName) {
        val bitmap by rememberAppIcon(app)
        val source = bitmap
        val icon by produceState<OceanoIcon?>(null, source) {
            value = if (source == null) null else withContext(Dispatchers.Default) { runCatching { shrink(source) }.getOrNull() }
        }
        icon
    }
}

private fun shrink(image: ImageBitmap): OceanoIcon {
    val w = image.width
    val h = image.height
    // Sample at most ~96 px a side: the target grid is 16x9, so more only costs memory.
    val k = maxOf(1, maxOf(w, h) / 96)
    val sw = w / k
    val sh = h / k
    val px = IntArray(w * h)
    image.readPixels(px, 0, 0, w, h)
    val small = IntArray(sw * sh) { px[(it / sw * k) * w + (it % sw) * k] }
    return OceanoIcon.fromArgb(small, sw, sh)
}
