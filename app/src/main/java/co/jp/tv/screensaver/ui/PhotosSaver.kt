package co.jp.tv.screensaver.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.PlaylistScheduler
import co.jp.tv.playlist.ui.LocalImageSource
import co.jp.tv.screensaver.domain.ScreensaverState
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SLIDE_MS = 15_000
private const val FADE_MS = 1_800
private const val MAX_WIDTH = 1920
private const val MAX_HEIGHT = 1080

/** One picture on screen with the direction of its slow pan. At most two exist at once: the one leaving and the one arriving. */
private class Slide(val id: Int, val image: ImageBitmap, val zoomIn: Boolean, val dirX: Float, val dirY: Float)

private val Pans = listOf(-1f to -1f, 1f to -1f, 1f to 1f, -1f to 1f)

/**
 * "Fotos": a slideshow with a slow Ken Burns pan and zoom, a new picture every 15 s dissolving into the
 * last one, and the small clock in a corner. Pictures are decoded bounded to the screen, one ahead of the
 * dissolve; the old one is dropped as soon as the new one is fully in. Paused, it holds the first picture still.
 */
@Composable
internal fun PhotosSaver(state: ScreensaverState) {
    val photos = state.photos
    val order = state.config.imagePlaylist.order
    val paused = LocalPaused.current
    val source = LocalImageSource.current
    val slides = remember(photos) { mutableStateListOf<Slide>() }
    // Where the slideshow is, kept across pause and resume so it does not jump back to the first picture.
    val position = remember(photos) { mutableIntStateOf(if (order == PlaylistOrder.SHUFFLE) Random.nextInt(photos.size) else 0) }

    LaunchedEffect(photos, paused) {
        val scheduler = PlaylistScheduler()
        var index = position.intValue.coerceIn(0, photos.size - 1)
        var counter = slides.lastOrNull()?.id?.plus(1) ?: 0
        while (true) {
            position.intValue = index
            val image = source.load(photos[index], MAX_WIDTH, MAX_HEIGHT)
            if (image != null) {
                val (dx, dy) = Pans[counter % Pans.size]
                while (slides.size > 1) slides.removeAt(0)
                slides += Slide(counter, image, zoomIn = counter % 2 == 0, dirX = dx, dirY = dy)
                counter++
            }
            if (paused) return@LaunchedEffect
            delay(FADE_MS.toLong())
            while (slides.size > 1) slides.removeAt(0)
            if (photos.size < 2) return@LaunchedEffect
            delay((SLIDE_MS - FADE_MS).toLong())
            index = scheduler.next(index, photos.size, order)
        }
    }

    Box(Modifier.fillMaxSize()) {
        slides.forEach { slide -> key(slide.id) { KenBurnsSlide(slide, paused) } }
        CornerClock(state)
    }
}

@Composable
private fun KenBurnsSlide(slide: Slide, paused: Boolean) {
    val fadeIn = remember { Animatable(0f) }
    val move = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (paused) {
            fadeIn.snapTo(1f)
            return@LaunchedEffect
        }
        coroutineScope {
            launch { move.animateTo(1f, tween(SLIDE_MS + FADE_MS, easing = LinearEasing)) }
            fadeIn.animateTo(1f, tween(FADE_MS))
        }
    }
    Image(
        bitmap = slide.image,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        // Everything animated is read in the layer block: no recomposition and no allocation per frame.
        modifier = Modifier.fillMaxSize().graphicsLayer {
            val p = move.value
            val zoom = if (slide.zoomIn) ZOOM_FROM + (ZOOM_TO - ZOOM_FROM) * p else ZOOM_TO - (ZOOM_TO - ZOOM_FROM) * p
            scaleX = zoom
            scaleY = zoom
            translationX = slide.dirX * size.width * PAN * (p - 0.5f)
            translationY = slide.dirY * size.height * PAN * (p - 0.5f)
            alpha = fadeIn.value
        },
    )
}

private const val ZOOM_FROM = 1.06f
private const val ZOOM_TO = 1.2f
private const val PAN = 0.04f
