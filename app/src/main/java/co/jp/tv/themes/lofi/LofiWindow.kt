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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
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

/**
 * The window: sky, sun or moon, clouds, skyline, lit windows, rain and frame. Sky, orb and
 * buildings switch at once like the artboard; clouds, lights and rain fade with [fade]
 * (0 = day, 1 = night) over the artboard's 2 s transition.
 */
@Composable
internal fun LofiWindow(night: Boolean, fade: State<Float>, millis: LongState, scatter: LofiScatter) {
    Spacer(
        Modifier
            .offset(x = 60.dp, y = 60.dp)
            .size(width = 380.dp, height = 260.dp)
            // box-shadow: 0 0 0 18px frame, 0 30px 80px rgba(0,0,0,0.5); the ring is on top.
            .lofiDropShadow(LofiPalette.Shadow, cornerRadius = 12.dp, offsetY = 15.dp, blur = 40.dp)
            .drawBehind {
                val ring = 9.dp.toPx()
                drawRoundRect(
                    LofiPalette.WindowFrame,
                    topLeft = Offset(-ring, -ring),
                    size = Size(size.width + 2 * ring, size.height + 2 * ring),
                    cornerRadius = CornerRadius(21.dp.toPx()),
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .drawWithCache {
                val nightSky = Brush.verticalGradient(
                    0f to LofiPalette.NightSkyTop,
                    0.55f to LofiPalette.NightSkyMid,
                    1f to LofiPalette.NightSkyBottom,
                    startY = 0f,
                    endY = WINDOW_H,
                )
                val daySky = Brush.verticalGradient(
                    0f to LofiPalette.DaySkyTop,
                    0.55f to LofiPalette.DaySkyMid,
                    1f to LofiPalette.DaySkyBottom,
                    startY = 0f,
                    endY = WINDOW_H,
                )
                // Night: 0 0 60px glow. Day: 0 0 90px 20px glow.
                val nightGlow = LofiGlow.brush(OrbCenter, ORB_RADIUS, 60f, LofiPalette.NightOrbGlow)
                val nightGlowExtent = LofiGlow.extent(ORB_RADIUS, 60f)
                val dayGlow = LofiGlow.brush(OrbCenter, ORB_RADIUS + 20f, 90f, LofiPalette.DayOrbGlow)
                val dayGlowExtent = LofiGlow.extent(ORB_RADIUS + 20f, 90f)
                val cloudPaths = Array(CloudW.size) { i ->
                    val w = CloudW[i]
                    val h = Math.round(w * 0.3f).toFloat()
                    Path().apply { addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(h / 2f))) }
                }
                onDrawBehind {
                    val ms = millis.longValue
                    val nightAmount = fade.value
                    inArtboardPx {
                        drawRect(if (night) nightSky else daySky, size = Size(WINDOW_W, WINDOW_H))
                        if (night) {
                            drawCircle(nightGlow, radius = nightGlowExtent, center = OrbCenter)
                            drawCircle(LofiPalette.NightOrb, radius = ORB_RADIUS, center = OrbCenter)
                        } else {
                            drawCircle(dayGlow, radius = dayGlowExtent, center = OrbCenter)
                            drawCircle(LofiPalette.DayOrb, radius = ORB_RADIUS, center = OrbCenter)
                        }

                        val cloudAmount = 1f - nightAmount
                        if (cloudAmount > 0f) {
                            for (i in CloudW.indices) {
                                val w = CloudW[i]
                                val h = Math.round(w * 0.3f).toFloat()
                                val dx = Math.round(w * 0.22f).toFloat()
                                val dy = -Math.round(w * 0.12f).toFloat()
                                val spread = Math.round(w * 0.02f).toFloat()
                                val alpha = CloudAlpha[i] * cloudAmount
                                val drift = CLOUD_DRIFT * JpMotion.InOut.transform(LofiMotion.alternate(ms, CloudPeriodMs[i]))
                                translate(left = CloudX[i] + drift, top = CloudY[i]) {
                                    // CSS clips an outer box-shadow to outside the box.
                                    clipPath(cloudPaths[i], clipOp = ClipOp.Difference) {
                                        drawRoundRect(
                                            LofiPalette.Cloud,
                                            topLeft = Offset(dx - spread, dy - spread),
                                            size = Size(w + 2 * spread, h + 2 * spread),
                                            cornerRadius = CornerRadius(h / 2f + spread),
                                            alpha = alpha,
                                        )
                                    }
                                    drawPath(cloudPaths[i], LofiPalette.Cloud, alpha = alpha)
                                }
                            }
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

                        if (nightAmount > 0f) {
                            for (i in 0 until LofiScatter.LIGHT_COUNT) {
                                drawRect(
                                    if (scatter.lightAmber[i]) LofiPalette.CityLightAmber else LofiPalette.CityLightPaper,
                                    topLeft = Offset(scatter.lightX[i], scatter.lightY[i]),
                                    size = Size(8f, 10f),
                                    alpha = 0.8f * nightAmount,
                                )
                            }
                            for (i in 0 until LofiScatter.RAIN_COUNT) {
                                // lo-rain: translateY(-120px) -> translateY(520px), linear.
                                val t = LofiMotion.loop(ms, scatter.rainPeriodMs[i], scatter.rainDelayMs[i])
                                drawRoundRect(
                                    LofiPalette.RainDrop,
                                    topLeft = Offset(scatter.rainX[i], -120f + 640f * t),
                                    size = Size(2f, scatter.rainHeight[i]),
                                    cornerRadius = CornerRadius(1f),
                                    alpha = nightAmount,
                                )
                            }
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
