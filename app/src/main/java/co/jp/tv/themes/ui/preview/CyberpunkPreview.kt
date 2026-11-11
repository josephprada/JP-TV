package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.chamferPath
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.drawGlowRect
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.unit

private val SkyTop = Color(0xFF05010F)
private val SkyMid = Color(0xFF0D0522)
private val SkyBottom = Color(0xFF1A0735)
private val TowerNear = Color(0xFF1A0A33)
private val TowerFar = Color(0xFF10061F)
private val Cyan = Color(0xFF19F0FF)
private val Yellow = Color(0xFFFCEE09)
private val SignFill = Color(0xFF0A0420)
private val Pink = Color(0xFFFF2A6D)
private val PinkText = Color(0xFFFFD7E6)

/**
 * Thumbnail of the Cyberpunk home: neon street with three signs. The artboard shows one static night
 * scene, so no day variant is drawn. The design title face (Chakra Petch) is not bundled; Orbitron is used.
 */
@Composable
internal fun CyberpunkPreview(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = unit()
            drawRect(cssLinear(180f, listOf(0f to SkyTop, 0.55f to SkyMid, 1f to SkyBottom)))
            drawRect(TowerNear, Offset(0f, size.height - 100f * u), Size(34f * u, 100f * u))
            drawRect(TowerFar, Offset(34f * u, size.height - 140f * u), Size(44f * u, 140f * u))
            drawRect(TowerNear, Offset(78f * u, size.height - 80f * u), Size(38f * u, 80f * u))
            drawRect(TowerFar, Offset(116f * u, size.height - 120f * u), Size(50f * u, 120f * u))
            // Neon pole: 3 x 70 px at left 70, bottom 90.
            val pole = Offset(70f * u, size.height - 90f * u - 70f * u)
            drawGlowRect(pole, Size(3f * u, 70f * u), 0f, 22f * u, Cyan.copy(alpha = 0.8f))
            drawRect(Cyan, pole, Size(3f * u, 70f * u))
            outlinedSign(size.width - 24f * u - 150f * u, 40f * u, 150f * u, 34f * u, u)
            drawPath(chamferPath(size.width - 40f * u - 170f * u, 88f * u, 170f * u, 40f * u, 9f * u), Yellow)
            outlinedSign(size.width - 24f * u - 150f * u, 142f * u, 150f * u, 34f * u, u)
        }
        BasicText(
            text = "JP-TV",
            modifier = Modifier.offset(x = 20.px, y = 24.px),
            style = TextStyle(
                color = PinkText,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontFamily = JpFonts.Orbitron,
                shadow = Shadow(Pink, blurRadius = 14f),
            ),
        )
    }
}

/** Cyan-bordered sign: 2 px of translucent cyan around a dark chamfered panel. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.outlinedSign(x: Float, y: Float, w: Float, h: Float, u: Float) {
    drawPath(chamferPath(x, y, w, h, 8f * u), Cyan.copy(alpha = 0.55f))
    drawPath(chamferPath(x + 2f * u, y + 2f * u, w - 4f * u, h - 4f * u, 7f * u), SignFill)
}
