package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.unit

private val RoomTop = Color(0xFF2A2350)
private val RoomWall = Color(0xFF1E1B3A)
private val RoomFloor = Color(0xFF4A3047)
private val WindowFrame = Color(0xFF3A2D4A)
private val SkyTop = Color(0xFF3B2D5C)
private val SkyMid = Color(0xFF7A4A72)
private val SkyBottom = Color(0xFFF2A65A)
private val Moon = Color(0xFFF6E7D0)
private val Paper = Color(0xFFF6E7D0)
private val PaperRule = Color(0xFFD8C7AD)
private val Ink = Color(0xFF2A2350)
private val Cat = Color(0xFF1B1530)

/**
 * Thumbnail of the Lo-fi home. The artboard shows one static scene (a night room with a window, a notebook
 * and a cat), so no day or night variant is drawn here.
 */
@Composable
internal fun LofiPreview(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = unit()
            drawRect(cssLinear(180f, listOf(0f to RoomTop, 0.7f to RoomWall, 0.7f to RoomFloor, 1f to RoomFloor)))
            // Window: 6 px frame ring around a dusk sky.
            val win = Offset(16f * u, 18f * u)
            val winSize = Size(170f * u, 120f * u)
            drawRoundRect(WindowFrame, Offset(win.x - 6f * u, win.y - 6f * u), Size(winSize.width + 12f * u, winSize.height + 12f * u), CornerRadius(16f * u))
            drawRoundRect(
                cssLinear(180f, listOf(0f to SkyTop, 0.55f to SkyMid, 1f to SkyBottom), winSize, win),
                win,
                winSize,
                CornerRadius(10f * u),
            )
            drawCircle(Moon, radius = 11f * u, center = Offset(140f * u + 11f * u, 32f * u + 11f * u))
            // Cat silhouette: 80x32 with 40/40/10/10 corner radii.
            val cat = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = 40f * u, top = 170f * u, right = 120f * u, bottom = 202f * u,
                        topLeftCornerRadius = CornerRadius(40f * u), topRightCornerRadius = CornerRadius(40f * u),
                        bottomRightCornerRadius = CornerRadius(10f * u), bottomLeftCornerRadius = CornerRadius(10f * u),
                    ),
                )
            }
            drawPath(cat, Cat)
            // Notebook: 150x170, rotated 2deg, ruled every 27 px.
            val noteLeft = size.width - 16f * u - 150f * u
            val noteTop = 22f * u
            rotate(2f, pivot = Offset(noteLeft + 75f * u, noteTop + 85f * u)) {
                drawRoundRect(Paper, Offset(noteLeft, noteTop), Size(150f * u, 170f * u), CornerRadius(8f * u))
                var y = 26f
                while (y < 170f) {
                    drawRect(PaperRule, Offset(noteLeft, noteTop + y * u), Size(150f * u, 1f * u))
                    y += 27f
                }
            }
        }
        BasicText(
            text = "✎ YouTube\n· Netflix\n· Spotify",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.px, top = 22.px)
                .size(width = 150.px, height = 170.px)
                .rotate(2f)
                .padding(horizontal = 14.px, vertical = 10.px),
            style = TextStyle(color = Ink, fontSize = 12.sp, lineHeight = 13.5.sp, fontFamily = JpFonts.Caveat),
        )
    }
}
