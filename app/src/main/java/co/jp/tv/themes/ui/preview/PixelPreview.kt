package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.drawGlowRect
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.unit

private val SkyTop = Color(0xFF1A1650)
private val SkyMid = Color(0xFF2B1F78)
private val SkyBottom = Color(0xFF0C0D2A)
private val GridLine = Color(0xFF8C82FF).copy(alpha = 0.5f)
private val Title = Color(0xFFFDC220)
private val SideCart = Color(0xFF82858C).copy(alpha = 0.8f)
private val SideScreenGreen = Color(0xFF38B764).copy(alpha = 0.8f)
private val SideScreenBlue = Color(0xFF3B82F6).copy(alpha = 0.8f)
private val HeroCart = Color(0xFFA3A6AD)
private val HeroScreen = Color(0xFFDC2626)
private val HeroGlow = Color(0xFFDC2626).copy(alpha = 0.7f)

/** Thumbnail of the Pixel home: console cartridges over a purple grid track. */
@Composable
internal fun PixelPreview(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = unit()
            drawRect(cssLinear(180f, listOf(0f to SkyTop, 0.6f to SkyMid, 1f to SkyBottom)))
            drawGrid(u)
            sideCart(left = 22f * u, color = SideScreenGreen, u = u)
            sideCart(left = size.width - 22f * u - 74f * u, color = SideScreenBlue, u = u)
            val heroLeft = size.width / 2f - 48f * u
            drawGlowRect(Offset(heroLeft, 52f * u), Size(96f * u, 112f * u), 10f * u, 24f * u, HeroGlow)
            drawGlowRect(Offset(heroLeft, 52f * u + 14f * u), Size(96f * u, 112f * u), 10f * u, 14f * u, Color.Black.copy(alpha = 0.5f))
            drawRoundRect(HeroCart, Offset(heroLeft, 52f * u), Size(96f * u, 112f * u), CornerRadius(10f * u))
            val screenTopLeft = Offset(heroLeft + 10f * u, 52f * u + 26f * u)
            val screenSize = Size(76f * u, 112f * u - 26f * u - 18f * u)
            drawRoundRect(HeroScreen, screenTopLeft, screenSize, CornerRadius(5f * u))
            // inset 3px white ring
            drawRoundRect(
                Color.White.copy(alpha = 0.9f),
                Offset(screenTopLeft.x + 1.5f * u, screenTopLeft.y + 1.5f * u),
                Size(screenSize.width - 3f * u, screenSize.height - 3f * u),
                CornerRadius(3.5f * u),
                style = Stroke(3f * u),
            )
        }
        BasicText(
            text = "JP-64",
            modifier = Modifier.offset(x = 14.px, y = 12.px),
            style = TextStyle(color = Title, fontSize = 10.sp, fontFamily = JpFonts.Fredoka),
        )
    }
}

/** Violet grid along the bottom 70 px: 2 px verticals every 28 px, 2 px horizontals every 18 px from the bottom. */
private fun DrawScope.drawGrid(u: Float) {
    val top = size.height - 70f * u
    var x = 0f
    while (x < size.width) {
        drawRect(GridLine, Offset(x, top), Size(2f * u, 70f * u))
        x += 28f * u
    }
    var y = size.height
    while (y > top) {
        drawRect(GridLine, Offset(0f, y - 2f * u), Size(size.width, 2f * u))
        y -= 18f * u
    }
}

private fun DrawScope.sideCart(left: Float, color: Color, u: Float) {
    drawRoundRect(SideCart, Offset(left, 76f * u), Size(74f * u, 86f * u), CornerRadius(8f * u))
    drawRoundRect(color, Offset(left + 8f * u, 76f * u + 18f * u), Size(58f * u, 86f * u - 18f * u - 14f * u), CornerRadius(4f * u))
}
