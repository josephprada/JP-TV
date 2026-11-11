package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer

/**
 * The sunlit window at the surface (`sw-sun`, 22 s) and the god rays fanning from (1150, -220)
 * (`sw-raysway` 19 s, each ray pulsing with `sw-ray`). Both dim at night (sun to 0.28, rays to 0.28).
 * The design blurs every ray by 16 px; here each ray is a soft wide pass under a sharper core.
 */
@Composable
internal fun OceanoSunlight(clock: OceanoClock, night: () -> Float, modifier: Modifier = Modifier) {
    val sunBrush = remember {
        Brush.radialGradient(
            0f to OceanoPalette.SunCore,
            0.35f to OceanoPalette.SunHalo,
            0.7f to OceanoPalette.SunEdge,
            center = SunCenter,
            radius = SUN_RX * 1.4142f,
        )
    }
    val rayBrush = remember {
        Brush.verticalGradient(
            0f to OceanoPalette.RayTop,
            0.55f to OceanoPalette.RayMid.copy(alpha = 0.18f),
            0.8f to OceanoPalette.RayEnd,
            startY = 0f,
            endY = RAY_LENGTH,
        )
    }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        val n = night()
        inDesignSpace {
            val sunAlpha = OceanoMotion.lerp(OceanoPalette.SunAlphaDay, OceanoPalette.SunAlphaNight, n)
            val drift = OceanoMotion.alternate(t, 22f)
            val sunScale = OceanoMotion.lerp(1f, 1.08f, drift)
            withTransform({
                translate(left = OceanoMotion.lerp(-90f, 90f, drift))
                scale(sunScale, sunScale * SUN_RY / SUN_RX, SunCenter)
            }) {
                drawCircle(sunBrush, radius = SUN_RX, center = SunCenter, alpha = sunAlpha)
            }

            val raysAlpha = OceanoMotion.lerp(OceanoPalette.RaysAlphaDay, OceanoPalette.RaysAlphaNight, n)
            val sway = OceanoMotion.lerp(-3.5f, 3.5f, OceanoMotion.alternate(t, 19f))
            withTransform({ rotate(sway, RayOrigin) }) {
                for (r in Rays.indices) {
                    val ray = Rays[r]
                    val beat = 0.35f + 0.55f * OceanoMotion.pulse(t, ray.periodS, ray.delayS)
                    val alpha = (ray.alpha * beat * raysAlpha).coerceIn(0f, 1f)
                    withTransform({
                        translate(RayOrigin.x, RayOrigin.y)
                        rotate(ray.angle, Offset.Zero)
                    }) {
                        drawRect(
                            rayBrush,
                            topLeft = Offset(-ray.width / 2f - 16f, 0f),
                            size = Size(ray.width + 32f, RAY_LENGTH),
                            alpha = alpha * 0.45f,
                        )
                        drawRect(
                            rayBrush,
                            topLeft = Offset(-ray.width / 2f, 0f),
                            size = Size(ray.width, RAY_LENGTH),
                            alpha = alpha * 0.6f,
                        )
                    }
                }
            }
        }
    }
}

private const val SUN_RX = 600f
private const val SUN_RY = 350f
private const val RAY_LENGTH = 1500f
private val SunCenter = Offset(1160f, -10f)
private val RayOrigin = Offset(1150f, -220f)

private class Ray(val width: Float, val angle: Float, val alpha: Float, val periodS: Float, val delayS: Float)

/** The artboard's fourteen rays: width, angle, top opacity, `sw-ray` period and delay. */
private val Rays = listOf(
    Ray(51f, -35.6f, 0.33f, 10.9f, 3.1f),
    Ray(97f, -28.6f, 0.41f, 8.2f, 4.7f),
    Ray(186f, -25.2f, 0.27f, 11.2f, 5.1f),
    Ray(107f, -18.8f, 0.35f, 8.5f, 1.4f),
    Ray(59f, -12.0f, 0.25f, 11.3f, 6.9f),
    Ray(68f, -6.1f, 0.34f, 10.2f, 8.6f),
    Ray(181f, -2.9f, 0.35f, 6.2f, 3.2f),
    Ray(123f, 2.4f, 0.22f, 6.8f, 6.0f),
    Ray(164f, 7.4f, 0.23f, 9.0f, 6.2f),
    Ray(116f, 13.1f, 0.33f, 12.0f, 3.0f),
    Ray(152f, 18.1f, 0.36f, 8.4f, 2.7f),
    Ray(178f, 22.5f, 0.26f, 6.5f, 4.3f),
    Ray(94f, 30.2f, 0.35f, 6.0f, 4.6f),
    Ray(183f, 33.1f, 0.22f, 11.9f, 10.0f),
)
