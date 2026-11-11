package co.jp.tv.themes.pixel

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.LocalPaused

private val Diamond = floatArrayOf(0.5f, 0f, 1f, 0.5f, 0.5f, 1f, 0f, 0.5f)
private val Triangle = floatArrayOf(0.5f, 0f, 1f, 1f, 0f, 1f)
private val Hexagon = floatArrayOf(0.25f, 0.05f, 0.75f, 0.05f, 1f, 0.5f, 0.75f, 0.95f, 0.25f, 0.95f, 0f, 0.5f)

/** One floating shape. Position and size are in design px (1920x1080); [points] are x,y fractions. */
private class ShapeSpec(
    val x: Int,
    val y: Int,
    val size: Int,
    val from: Color,
    val to: Color,
    val points: FloatArray,
    val spinSeconds: Int,
    val delaySeconds: Float,
)

private val Specs = listOf(
    ShapeSpec(220, 170, 84, PixelPalette.Red, PixelPalette.RedDark, Diamond, 9, 0f),
    ShapeSpec(560, 120, 64, PixelPalette.Blue, PixelPalette.BlueDark, Triangle, 11, 1.5f),
    ShapeSpec(1230, 150, 72, PixelPalette.Green, PixelPalette.GreenDark, Hexagon, 10, 0.8f),
    ShapeSpec(1580, 190, 90, PixelPalette.Gold, PixelPalette.GoldDark, Diamond, 12, 2.2f),
    ShapeSpec(140, 760, 70, PixelPalette.Green, PixelPalette.GreenDark, Triangle, 13, 1f),
    ShapeSpec(1700, 740, 80, PixelPalette.Red, PixelPalette.RedDark, Hexagon, 11, 0.3f),
    ShapeSpec(420, 840, 56, PixelPalette.Gold, PixelPalette.GoldDark, Diamond, 10, 2f),
)

/** The seven floating, spinning shapes behind the shelf. Static while paused. */
@Composable
internal fun PixelShapes() {
    val paused = LocalPaused.current
    Box(Modifier.fillMaxSize()) {
        Specs.forEach { spec ->
            if (paused) {
                PixelShape(spec, lift = { 0f }, spin = { 0f })
            } else {
                AnimatedPixelShape(spec)
            }
        }
    }
}

@Composable
private fun AnimatedPixelShape(spec: ShapeSpec) {
    val transition = rememberInfiniteTransition(label = "pixelShape")
    // pk-float: up 18px and back over spinSeconds / 2, after the shape's own delay.
    val lift by transition.animateFloat(
        0f,
        -9f,
        infiniteRepeatable(
            tween(spec.spinSeconds * 250, easing = JpMotion.InOut),
            RepeatMode.Reverse,
            initialStartOffset = StartOffset((spec.delaySeconds * 1000).toInt()),
        ),
        label = "lift",
    )
    // pk-spin: a full turn around the vertical axis.
    val spin by transition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(spec.spinSeconds * 1000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    PixelShape(spec, lift = { lift }, spin = { spin })
}

@Composable
private fun PixelShape(spec: ShapeSpec, lift: () -> Float, spin: () -> Float) {
    Box(
        Modifier
            .offset((spec.x / 2f).dp, (spec.y / 2f).dp)
            .size((spec.size / 2f).dp)
            .graphicsLayer {
                alpha = 0.7f
                translationY = lift().dp.toPx()
            },
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = spin()
                    cameraDistance = 14f * density
                }
                .drawWithCache {
                    val path = Path().apply {
                        val p = spec.points
                        moveTo(p[0] * size.width, p[1] * size.height)
                        var i = 2
                        while (i < p.size) {
                            lineTo(p[i] * size.width, p[i + 1] * size.height)
                            i += 2
                        }
                        close()
                    }
                    val brush = cssGradient(
                        135f,
                        size,
                        Offset.Zero,
                        listOf(0f to spec.from, 1f to spec.to),
                    )
                    onDrawBehind { drawPath(path, brush) }
                },
        )
    }
}
