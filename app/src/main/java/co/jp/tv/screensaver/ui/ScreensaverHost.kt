package co.jp.tv.screensaver.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.playlist.ui.LocalPlaylistStateStore
import co.jp.tv.playlist.ui.LocalWallpaperMedia
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverModes
import co.jp.tv.screensaver.domain.ScreensaverState
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val VEIL_MS = 450

/**
 * The saver as the overlay and the DreamService show it: resolves the chosen mode (for "Aleatorio", one of the
 * others, changing every ten minutes) and dissolves through black between modes, so two players or two heavy
 * scenes are never on screen together.
 */
@Composable
fun ScreensaverHost(state: ScreensaverState, modifier: Modifier = Modifier) {
    val chosen = state.config.screensaver.mode
    val hasPhotos = state.photos.isNotEmpty()
    val hasVideos = state.videos.items.isNotEmpty()
    var mode by remember { mutableStateOf(ScreensaverModes.concrete(chosen, null, hasPhotos, hasVideos, Random::nextInt)) }
    val veil = remember { Animatable(0f) }

    suspend fun switchTo(next: ScreensaverMode) {
        if (next == mode) return
        veil.animateTo(1f, tween(VEIL_MS))
        mode = next
        veil.animateTo(0f, tween(VEIL_MS))
    }

    // A change in the settings (or in what is available) applies at once.
    LaunchedEffect(chosen, hasPhotos, hasVideos) {
        val stillValid = if (chosen == ScreensaverMode.RANDOM) {
            (mode != ScreensaverMode.PHOTOS || hasPhotos) && (mode != ScreensaverMode.LIVE || hasVideos)
        } else {
            mode == chosen
        }
        if (!stillValid) switchTo(ScreensaverModes.concrete(chosen, mode, hasPhotos, hasVideos, Random::nextInt))
    }
    val paused = LocalPaused.current
    LaunchedEffect(chosen, paused, hasPhotos, hasVideos) {
        if (chosen != ScreensaverMode.RANDOM || paused) return@LaunchedEffect
        while (true) {
            delay(ScreensaverModes.RANDOM_EVERY_MS)
            switchTo(ScreensaverModes.concrete(ScreensaverMode.RANDOM, mode, hasPhotos, hasVideos, Random::nextInt))
        }
    }

    // The saver never remembers a slot of its own: it must not move the home wallpaper's playlist along.
    CompositionLocalProvider(LocalWallpaperMedia provides state.wallpaperMedia, LocalPlaylistStateStore provides null) {
        Box(modifier.fillMaxSize().background(Color.Black)) {
            ScreensaverContent(mode, state)
            Box(Modifier.fillMaxSize().drawBehind { drawRect(Color.Black, alpha = veil.value) })
        }
    }
}
