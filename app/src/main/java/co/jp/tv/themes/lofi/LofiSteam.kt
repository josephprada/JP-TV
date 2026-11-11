package co.jp.tv.themes.lofi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val STEAM_PERIOD_MS = 3200

/** Two steam puffs over the mug (lo-steam, 3.2 s ease-out, the second 1.4 s later). */
@Composable
internal fun LofiSteam(millis: LongState) {
    SteamPuff(x = 442.5.dp, y = 335.dp, width = 8.dp, height = 25.dp, delayMs = 0, millis = millis)
    SteamPuff(x = 456.dp, y = 338.dp, width = 7.dp, height = 22.dp, delayMs = 1400, millis = millis)
}

@Composable
private fun SteamPuff(x: Dp, y: Dp, width: Dp, height: Dp, delayMs: Int, millis: LongState) {
    Box(
        Modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .graphicsLayer {
                // Transform keyframes 0 % and 100 %; opacity 0 -> 0.7 at 20 % -> 0.
                val t = LofiMotion.loop(millis.longValue, STEAM_PERIOD_MS, delayMs)
                val move = LofiMotion.EaseOut.transform(t)
                translationY = -60.dp.toPx() * move
                scaleX = 1f + 0.8f * move
                alpha = if (t < 0.2f) {
                    0.7f * LofiMotion.EaseOut.transform(t / 0.2f)
                } else {
                    0.7f * (1f - LofiMotion.EaseOut.transform((t - 0.2f) / 0.8f))
                }
            }
            // CSS blur(6px) = sigma 3 dp; RenderEffect takes a radius (sigma ~ 0.577 r). API 31+ only.
            .blur(5.dp, BlurredEdgeTreatment.Unbounded)
            .background(LofiPalette.Steam, RoundedCornerShape(5.dp)),
    )
}
