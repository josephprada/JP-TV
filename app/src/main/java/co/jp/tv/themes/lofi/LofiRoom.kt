package co.jp.tv.themes.lofi

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.runtime.remember
import co.jp.tv.core.design.bakedLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import co.jp.tv.core.design.JpMotion

// Artboard px, absolute on the 1920x1080 artboard.
private const val DESK_TOP = 760f
private val LampCenter = Offset(1140f, 780f)

// Lamp glow box 640x520, `radial-gradient(ellipse, ...)` = farthest-corner ellipse, fading out at 65 %.
private const val LAMP_GLOW_RX = 320f * 1.4142135f
private const val LAMP_GLOW_RY = 260f * 1.4142135f

// Cat body radii "110px 110px 30px 30px" on a 230x90 box; CSS scales them by 90 / 140.
private const val CAT_TOP_RADIUS = 110f * 90f / 140f
private const val CAT_BOTTOM_RADIUS = 30f * 90f / 140f
private val TailPivot = Offset(340f, 751f)

/**
 * The room under the window, in artboard order: desk, sunbeam (day), lamp glow (night),
 * lamp, mug, sleeping cat. The desk switches at once; sunbeam and glow fade with [fade]
 * (0 = day, 1 = night). The mug steam is [LofiSteam], drawn right after this.
 */
@Composable
internal fun LofiRoom(night: Boolean, fade: State<Float>, millis: LongState) {
    val shapes = remember { RoomShapes() }
    // Desk: baked per phase, below the light.
    Spacer(
        Modifier
            .fillMaxSize()
            .bakedLayer(topFraction = DESK_TOP / 1080f) {
                inArtboardPx {
                    val boardH = 1080f
                    val desk = if (night) {
                        Brush.verticalGradient(0f to LofiPalette.NightDeskTop, 1f to LofiPalette.NightDeskBottom, startY = DESK_TOP, endY = boardH)
                    } else {
                        Brush.verticalGradient(0f to LofiPalette.DayDeskTop, 1f to LofiPalette.DayDeskBottom, startY = DESK_TOP, endY = boardH)
                    }
                    drawRect(desk, topLeft = Offset(0f, DESK_TOP), size = Size(size.width / (density / 2f), boardH - DESK_TOP))
                }
            },
    )
    // Light: the sunbeam by day and the flickering lamp glow at night, the only animated fills.
    // Own layer: these two read the frame clock in draw, which would otherwise re-record the whole screen every frame.
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawWithCache {
                val sunbeamBrush = Brush.verticalGradient(
                    0f to LofiPalette.SunbeamTop,
                    1f to LofiPalette.SunbeamBottom,
                    startY = 640f,
                    endY = 980f,
                )
                val lampGlow = Brush.radialGradient(
                    0f to LofiPalette.LampGlowCore,
                    1f to LofiPalette.LampGlowEdge,
                    center = LampCenter,
                    radius = 0.65f * LAMP_GLOW_RY,
                )
                onDrawBehind {
                    val nightAmount = fade.value
                    inArtboardPx {
                        if (nightAmount < 1f) {
                            drawPath(shapes.sunbeam, sunbeamBrush, alpha = 1f - nightAmount)
                        }
                        if (nightAmount > 0f) {
                            val glowAlpha = nightAmount * lampFlicker(millis.longValue)
                            scale(scaleX = LAMP_GLOW_RX / LAMP_GLOW_RY, scaleY = 1f, pivot = LampCenter) {
                                drawCircle(lampGlow, radius = 0.65f * LAMP_GLOW_RY, center = LampCenter, alpha = glowAlpha)
                            }
                        }
                    }
                }
            },
    )
    // Lamp, mug and cat: static, baked into the band they occupy.
    Spacer(
        Modifier
            .fillMaxSize()
            .bakedLayer(topFraction = 540f / 1080f, bottomFraction = 800f / 1080f) {
                inArtboardPx {
                    drawPath(shapes.shade, LofiPalette.LampShade)
                    drawRect(LofiPalette.LampStem, topLeft = Offset(1144f, 640f), size = Size(12f, 130f))
                    drawPath(shapes.mug, LofiPalette.Paper)
                    drawPath(shapes.handle, LofiPalette.Paper)
                    drawPath(shapes.catBody, LofiPalette.Cat)
                    drawOval(LofiPalette.Cat, topLeft = Offset(470f, 668f), size = Size(80f, 70f))
                    drawPath(shapes.ears, LofiPalette.Cat)
                }
            },
    )
    // Tail: the only moving part of the cat.
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawBehind {
                inArtboardPx {
                    rotate(degrees = tailAngle(millis.longValue), pivot = TailPivot) {
                        drawRoundRect(
                            LofiPalette.Cat,
                            topLeft = Offset(230f, 740f),
                            size = Size(110f, 22f),
                            cornerRadius = CornerRadius(11f),
                        )
                    }
                }
            },
    )
}

/** Outlines of the room's objects, built once. */
private class RoomShapes {
    // clip-path: polygon(0 0, 80% 0, 100% 100%, 22% 100%) on a 900x340 box at (140, 640).
    val sunbeam = Path().apply {
        moveTo(140f, 640f)
        lineTo(140f + 720f, 640f)
        lineTo(140f + 900f, 980f)
        lineTo(140f + 198f, 980f)
        close()
    }
    val shade = Path().apply {
        addRoundRect(
            RoundRect(
                1080f, 560f, 1220f, 640f,
                topLeftCornerRadius = CornerRadius(70f),
                topRightCornerRadius = CornerRadius(70f),
                bottomRightCornerRadius = CornerRadius(10f),
                bottomLeftCornerRadius = CornerRadius(10f),
            ),
        )
    }
    val mug = Path().apply {
        addRoundRect(
            RoundRect(
                860f, 700f, 950f, 790f,
                bottomRightCornerRadius = CornerRadius(22f),
                bottomLeftCornerRadius = CornerRadius(22f),
            ),
        )
    }
    // Handle: content-box 36x44 with a 10 px border on top, right and bottom.
    val handle = Path().apply {
        fillType = PathFillType.EvenOdd
        addRoundRect(
            RoundRect(
                938f, 716f, 984f, 780f,
                topRightCornerRadius = CornerRadius(22f),
                bottomRightCornerRadius = CornerRadius(22f),
            ),
        )
        addRoundRect(
            RoundRect(
                938f, 726f, 974f, 770f,
                topRightCornerRadius = CornerRadius(12f),
                bottomRightCornerRadius = CornerRadius(12f),
            ),
        )
    }
    val catBody = Path().apply {
        addRoundRect(
            RoundRect(
                300f, 690f, 530f, 780f,
                topLeftCornerRadius = CornerRadius(CAT_TOP_RADIUS),
                topRightCornerRadius = CornerRadius(CAT_TOP_RADIUS),
                bottomRightCornerRadius = CornerRadius(CAT_BOTTOM_RADIUS),
                bottomLeftCornerRadius = CornerRadius(CAT_BOTTOM_RADIUS),
            ),
        )
    }
    val ears = Path().apply {
        moveTo(482f, 678f)
        lineTo(496f, 652f)
        lineTo(510f, 678f)
        close()
        moveTo(516f, 678f)
        lineTo(530f, 652f)
        lineTo(544f, 678f)
        close()
    }
}

/** lo-lamp, 7 s ease-in-out: 0.85 until 48 %, 0.7 at 50 %, 0.88 at 52 %, back to 0.85. */
private fun lampFlicker(ms: Long): Float {
    val t = LofiMotion.loop(ms, 7000) * 7000f
    return when {
        t < 3360f -> 0.85f
        t < 3500f -> segment(0.85f, 0.7f, (t - 3360f) / 140f)
        t < 3640f -> segment(0.7f, 0.88f, (t - 3500f) / 140f)
        else -> segment(0.88f, 0.85f, (t - 3640f) / 3360f)
    }
}

/** lo-tail, 4 s ease-in-out: -8 deg -> 10 deg -> -8 deg around the tail's right edge. */
private fun tailAngle(ms: Long): Float {
    val t = LofiMotion.loop(ms, 4000)
    return if (t < 0.5f) segment(-8f, 10f, t * 2f) else segment(10f, -8f, (t - 0.5f) * 2f)
}

private fun segment(from: Float, to: Float, fraction: Float): Float =
    from + (to - from) * JpMotion.InOut.transform(fraction)
