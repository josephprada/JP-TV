package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Draws one group of [OceanoSwimmer]s at the clock's time. [alpha] is read at draw time so the
 * day/night crossfade (reef fish to 0.5, schools to 0) never recomposes; at 0 nothing is drawn.
 */
@Composable
internal fun OceanoFishLayer(
    clock: OceanoClock,
    fish: List<OceanoSwimmer>,
    alpha: () -> Float = { 1f },
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val layerAlpha = alpha()
        if (layerAlpha <= 0.001f) return@Canvas
        val t = clock.seconds
        inDesignSpace {
            for (i in fish.indices) fish[i].draw(this, t, layerAlpha)
        }
    }
}
