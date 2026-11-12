package co.jp.tv.themes.lofi

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import co.jp.tv.core.design.bakedLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import co.jp.tv.core.design.JpMotion

// Artboard px, relative to the window (760x520).
private const val WINDOW_W = 760f
private const val WINDOW_H = 520f
private val OrbCenter = Offset(565f, 115f)
private const val ORB_RADIUS = 45f

// Clouds: [x, y, w, opacity, seconds] from the artboard; h = round(w * 0.3), shadow from w.
private val CloudX = floatArrayOf(60f, 300f, 560f)
private val CloudY = floatArrayOf(60f, 150f, 220f)
private val CloudW = floatArrayOf(200f, 150f, 180f)
private val CloudAlpha = floatArrayOf(0.95f, 0.8f, 0.85f)
private val CloudPeriodMs = intArrayOf(34_000, 46_000, 40_000)
private const val CLOUD_DRIFT = 90f

// Skyline, flex row with 6 px gaps; 762 px of content shrink by 2 px in proportion to width.
private val BuildingW = floatArrayOf(90f, 70f, 110f, 60f, 120f, 80f, 100f, 90f)
private val BuildingH = floatArrayOf(150f, 200f, 120f, 180f, 140f, 210f, 160f, 130f)
private const val BUILDING_GAP = 6f
private const val BUILDING_SHRINK = 2f / 720f

private const val FRAME_BAR = 14f

// Weather: how strongly the overcast veil and the dark night clouds show, and the lightning flash.
private const val VEIL_GAIN = 1.4f
private const val NIGHT_CLOUD_ALPHA = 0.7f
private const val FLASH_ALPHA = 0.5f

/**
 * The window: sky, sun or moon, clouds, skyline, lit windows, rain and frame. Sky, orb, buildings and
 * the weather's veil and haze switch at once like the artboard and are baked into a bitmap per phase
 * and [sky] (with the frame ring and its shadow); clouds, lights, rain and lightning fade with [fade]
 * (0 = day, 1 = night) over the artboard's 2 s transition and are the only part redrawn on a clock tick.
 */
@Composable
internal fun LofiWindow(
    night: Boolean,
    fade: State<Float>,
    millis: LongState,
    scatter: LofiScatter,
    sky: LofiSky = LofiSky.Artboard,
    lowEnd: Boolean = false,
) {
    val cloudShapes = remember { CloudShapes() }
    // Static: shadow, frame ring, sky, orb and skyline. The window sits at (60, 60) dp, so everything below
    // y = 0.8 of the screen is empty and left out of the bitmap.
    Spacer(
        Modifier
            .fillMaxSize()
            .bakedLayer(bottomFraction = 0.8f) {
                val k = density / 2f
                translate(left = 60.dp.toPx(), top = 60.dp.toPx()) {
                    // box-shadow: 0 0 0 18px frame, 0 30px 80px rgba(0,0,0,0.5); the ring is on top.
                    drawLofiDropShadow(
                        LofiPalette.Shadow,
                        width = 380.dp.toPx(),
                        height = 260.dp.toPx(),
                        cornerRadius = 12.dp.toPx(),
                        offsetY = 15.dp.toPx(),
                        blur = 40.dp.toPx(),
                    )
                    val ring = 9.dp.toPx()
                    drawRoundRect(
                        LofiPalette.WindowFrame,
                        topLeft = Offset(-ring, -ring),
                        size = Size(380.dp.toPx() + 2 * ring, 260.dp.toPx() + 2 * ring),
                        cornerRadius = CornerRadius(21.dp.toPx()),
                    )
                    clipPath(Path().apply { addRoundRect(RoundRect(0f, 0f, 380.dp.toPx(), 260.dp.toPx(), CornerRadius(12.dp.toPx()))) }) {
                        scale(scaleX = k, scaleY = k, pivot = Offset.Zero) { drawWindowBackdrop(night, sky) }
                    }
                }
            },
    )
    // Dynamic: clouds, lit windows, rain and the frame bars, clipped to the rounded window.
    Spacer(
        Modifier
            .offset(x = 60.dp, y = 60.dp)
            .size(width = 380.dp, height = 260.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawWithCache {
                onDrawBehind {
                    val ms = millis.longValue
                    val nightAmount = fade.value
                    inArtboardPx {
                        val cloudAmount = (1f - nightAmount) * sky.clouds
                        if (cloudAmount > 0f) {
                            for (i in CloudW.indices) {
                                val drift = CLOUD_DRIFT * JpMotion.InOut.transform(LofiMotion.alternate(ms, CloudPeriodMs[i]))
                                translate(left = CloudX[i] + drift, top = CloudY[i]) {
                                    drawPath(cloudShapes.paths[i], LofiPalette.Cloud, alpha = CloudAlpha[i] * cloudAmount)
                                }
                            }
                        }
                        // Overcast nights: dark clouds drifting over the moon.
                        val nightCloudAmount = nightAmount * sky.overcast
                        if (nightCloudAmount > 0f) {
                            for (i in CloudW.indices) {
                                val drift = CLOUD_DRIFT * JpMotion.InOut.transform(LofiMotion.alternate(ms, CloudPeriodMs[i]))
                                translate(left = CloudX[i] + drift, top = CloudY[i]) {
                                    drawPath(cloudShapes.paths[i], LofiPalette.NightCloud, alpha = NIGHT_CLOUD_ALPHA * nightCloudAmount)
                                }
                            }
                        }
                        if (nightAmount > 0f) {
                            for (i in 0 until LofiScatter.LIGHT_COUNT) {
                                drawRect(
                                    if (scatter.lightAmber[i]) LofiPalette.CityLightAmber else LofiPalette.CityLightPaper,
                                    topLeft = Offset(scatter.lightX[i], scatter.lightY[i]),
                                    size = Size(8f, 10f),
                                    alpha = 0.8f * nightAmount,
                                )
                            }
                        }
                        // Rain falls in either phase when the weather says so; the day and night streaks fade with the phase.
                        val dayDrops = LofiSky.dropCount(sky.rainDay, LofiScatter.RAIN_COUNT, lowEnd)
                        if (dayDrops > 0 && nightAmount < 1f) drawRain(scatter, dayDrops, ms, LofiPalette.RainDropDay, 1f - nightAmount)
                        val nightDrops = LofiSky.dropCount(sky.rainNight, LofiScatter.RAIN_COUNT, lowEnd)
                        if (nightDrops > 0 && nightAmount > 0f) drawRain(scatter, nightDrops, ms, LofiPalette.RainDrop, nightAmount)
                        if (sky.lightning) {
                            val flash = LofiSky.flash(ms)
                            if (flash > 0f) drawRect(LofiPalette.Flash, size = Size(WINDOW_W, WINDOW_H), alpha = FLASH_ALPHA * flash)
                        }
                        drawRect(
                            LofiPalette.WindowFrame,
                            topLeft = Offset(WINDOW_W / 2f - FRAME_BAR / 2f, 0f),
                            size = Size(FRAME_BAR, WINDOW_H),
                        )
                        drawRect(
                            LofiPalette.WindowFrame,
                            topLeft = Offset(0f, WINDOW_H / 2f - FRAME_BAR / 2f),
                            size = Size(WINDOW_W, FRAME_BAR),
                        )
                    }
                }
            },
    )
}

/** [count] rain streaks, each looping down the window on its own period (lo-rain: translateY(-120px) -> translateY(520px), linear). */
private fun DrawScope.drawRain(scatter: LofiScatter, count: Int, ms: Long, color: Color, alpha: Float) {
    for (i in 0 until count) {
        val t = LofiMotion.loop(ms, scatter.rainPeriodMs[i], scatter.rainDelayMs[i])
        // 2 px wide: a plain rect reads the same as a 1 px radius and skips the rounded-rect op.
        drawRect(color, topLeft = Offset(scatter.rainX[i], -120f + 640f * t), size = Size(2f, scatter.rainHeight[i]), alpha = alpha)
    }
}

/** Sky, sun or moon with its glow, the weather's veil, the skyline and the haze over it, in window artboard px. */
private fun DrawScope.drawWindowBackdrop(night: Boolean, weather: LofiSky) {
    val sky = Brush.verticalGradient(
        0f to if (night) LofiPalette.NightSkyTop else LofiPalette.DaySkyTop,
        0.55f to if (night) LofiPalette.NightSkyMid else LofiPalette.DaySkyMid,
        1f to if (night) LofiPalette.NightSkyBottom else LofiPalette.DaySkyBottom,
        startY = 0f,
        endY = WINDOW_H,
    )
    drawRect(sky, size = Size(WINDOW_W, WINDOW_H))
    if (night) {
        // 0 0 60px glow.
        drawCircle(LofiGlow.brush(OrbCenter, ORB_RADIUS, 60f, LofiPalette.NightOrbGlow), radius = LofiGlow.extent(ORB_RADIUS, 60f), center = OrbCenter)
        drawCircle(LofiPalette.NightOrb, radius = ORB_RADIUS, center = OrbCenter)
    } else {
        // 0 0 90px 20px glow.
        drawCircle(LofiGlow.brush(OrbCenter, ORB_RADIUS + 20f, 90f, LofiPalette.DayOrbGlow), radius = LofiGlow.extent(ORB_RADIUS + 20f, 90f), center = OrbCenter)
        drawCircle(LofiPalette.DayOrb, radius = ORB_RADIUS, center = OrbCenter)
    }
    // Overcast: the sun or moon sinks behind a grey veil.
    if (weather.overcast > 0f) {
        drawRect(if (night) LofiPalette.NightVeil else LofiPalette.DayVeil, size = Size(WINDOW_W, WINDOW_H), alpha = weather.overcast * VEIL_GAIN)
    }
    var x = 0f
    for (i in BuildingW.indices) {
        val w = BuildingW[i] * (1f - BUILDING_SHRINK)
        val h = BuildingH[i]
        val color = when {
            i % 2 == 0 && night -> LofiPalette.NightBuildingA
            i % 2 == 0 -> LofiPalette.DayBuildingA
            night -> LofiPalette.NightBuildingB
            else -> LofiPalette.DayBuildingB
        }
        drawRect(color, topLeft = Offset(x, WINDOW_H - h), size = Size(w, h))
        x += w + BUILDING_GAP
    }
    // Fog: the lower skyline dissolves into haze.
    if (weather.fog > 0f) {
        val haze = if (night) LofiPalette.NightFog else LofiPalette.DayFog
        drawRect(
            Brush.verticalGradient(0f to haze.copy(alpha = 0f), 1f to haze.copy(alpha = weather.fog), startY = WINDOW_H * 0.15f, endY = WINDOW_H),
            size = Size(WINDOW_W, WINDOW_H),
        )
    }
}

/**
 * Each cloud as one outline: the pill plus its offset, spread copy (the artboard's `box-shadow`),
 * merged so it draws with a single alpha and needs no difference clip.
 */
private class CloudShapes {
    val paths: Array<Path> = Array(CloudW.size) { i ->
        val w = CloudW[i]
        val h = Math.round(w * 0.3f).toFloat()
        val dx = Math.round(w * 0.22f).toFloat()
        val dy = -Math.round(w * 0.12f).toFloat()
        val spread = Math.round(w * 0.02f).toFloat()
        val pill = Path().apply { addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(h / 2f))) }
        val puff = Path().apply {
            addRoundRect(RoundRect(dx - spread, dy - spread, dx - spread + w + 2 * spread, dy - spread + h + 2 * spread, CornerRadius(h / 2f + spread)))
        }
        Path.combine(PathOperation.Union, pill, puff)
    }
}
