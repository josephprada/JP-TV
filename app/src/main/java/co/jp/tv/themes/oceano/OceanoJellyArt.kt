package co.jp.tv.themes.oceano

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Geometry shared by every moon jelly, in the artboard's 200x360 jelly box: the bell outline
 * (`border-radius: 50% 50% 18% 18% / 70% 70% 30% 30%` on 140x104 at (30, 0)), tentacle layout and
 * the strokes used to draw them. Per-tint brushes come from [brushesFor].
 */
internal class OceanoJellyArt private constructor() {
    val bell: Path = Path().apply {
        addRoundRect(
            RoundRect(
                left = BELL_LEFT,
                top = 0f,
                right = BELL_LEFT + BELL_WIDTH,
                bottom = BELL_HEIGHT,
                topLeftCornerRadius = CornerRadius(70f, 72.8f),
                topRightCornerRadius = CornerRadius(70f, 72.8f),
                bottomRightCornerRadius = CornerRadius(25.2f, 31.2f),
                bottomLeftCornerRadius = CornerRadius(25.2f, 31.2f),
            ),
        )
    }

    /** Thin tentacles: x in the jelly box, segment length, `sw-seg` (1) or `sw-seg2` (2), period. */
    val thinX = floatArrayOf(38f, 53f, 68f, 83f, 100f, 117f, 132f, 147f, 162f)
    val thinLength = floatArrayOf(32f, 38f, 32f, 39f, 35f, 37f, 40f, 39f, 37f)
    val thinKind = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
    val thinPeriod = floatArrayOf(3.05f, 3.33f, 3.18f, 3.08f, 2.74f, 2.97f, 2.61f, 3.05f, 3.24f)
    val thinWidths = floatArrayOf(2f, 2f, 1.8f, 1.6f, 1.4f, 1.2f, 1f)

    /** Oral arms: three striped white ribbons (`jf-seg3`). */
    val armX = floatArrayOf(86f, 100f, 114f)
    val armPeriod = floatArrayOf(4.07f, 4.16f, 3.40f)
    val armWidths = floatArrayOf(10f, 9f, 8f, 7f, 5f)
    val armStripes: PathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))

    val borderIdle = Stroke(width = 1.5f)
    val gonadStroke = Stroke(width = 3f, cap = StrokeCap.Round)
    val rimStroke = Stroke(width = 2f, cap = StrokeCap.Round)

    /** Soft halo around the bell (`box-shadow 0 0 22px` idle, `0 0 70px` focused), three passes each. */
    val haloIdle = listOf(Stroke(width = 7f), Stroke(width = 14f), Stroke(width = 22f))
    val haloFocused = listOf(Stroke(width = 20f), Stroke(width = 42f), Stroke(width = 70f))

    class TintBrushes(val glow: Brush, val bellIdle: Brush, val bellFocused: Brush)

    fun brushesFor(tint: Color): TintBrushes = TintBrushes(
        glow = Brush.radialGradient(
            0f to tint,
            0.36f to tint.copy(alpha = 0.33f),
            0.66f to tint.copy(alpha = 0f),
            center = GlowCenter,
            radius = 260f,
        ),
        bellIdle = bellBrush(tint, white = 0.6f, inner = 0.35f, outer = 0.18f),
        bellFocused = bellBrush(tint, white = 0.85f, inner = 0.55f, outer = 0.3f),
    )

    private fun bellBrush(tint: Color, white: Float, inner: Float, outer: Float): Brush = Brush.radialGradient(
        0f to OceanoPalette.White.copy(alpha = white),
        0.4f to tint.copy(alpha = inner),
        0.75f to tint.copy(alpha = outer),
        1f to tint.copy(alpha = 0.12f),
        center = Offset(BELL_LEFT + BELL_WIDTH / 2f, BELL_HEIGHT * 0.24f),
        radius = 105f,
    )

    companion object {
        const val BOX_WIDTH = 200f
        const val BOX_HEIGHT = 360f
        const val BELL_LEFT = 30f
        const val BELL_WIDTH = 140f
        const val BELL_HEIGHT = 104f
        const val TENTACLE_TOP = 100f

        /** Origin of the focus scale (`transform-origin: 50% 55px`) and centre of the glow. */
        val GlowCenter = Offset(100f, 55f)
        val BellCenter = Offset(BELL_LEFT + BELL_WIDTH / 2f, BELL_HEIGHT / 2f)

        fun build(): OceanoJellyArt = OceanoJellyArt()
    }
}
