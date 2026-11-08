package co.jp.tv.themes.jptv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.LocalAppIcons

private val RowShape = RoundedCornerShape(12.dp)
private val TileShape = RoundedCornerShape(8.dp)

@Composable
internal fun AppRow(app: TvApp, focused: Boolean, modifier: Modifier = Modifier) {
    val rowModifier = if (focused) {
        Modifier
            .graphicsLayer { scaleX = 1.03f; scaleY = 1.03f }
            .background(JpTvPalette.FocusFill, RowShape)
            .border(0.5.dp, JpTvPalette.Accent.copy(alpha = 0.7f), RowShape)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .then(rowModifier)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        AppTile(app, focused)
        BasicText(
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = if (focused) JpTvPalette.Paper else JpTvPalette.Muted,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun AppTile(app: TvApp, focused: Boolean) {
    val icons = LocalAppIcons.current
    val icon by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, app.packageName) {
        value = icons.load(app)
    }
    val base = Modifier.size(27.dp).clip(TileShape)
    val styled = if (focused) {
        base.background(JpTvPalette.Accent)
    } else {
        base.background(JpTvPalette.TileFill).border(0.5.dp, JpTvPalette.Line, TileShape)
    }
    Box(styled, contentAlignment = Alignment.Center) {
        val bitmap = icon
        if (bitmap != null) {
            Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(21.dp))
        } else {
            BasicText(
                text = initials(app.label),
                style = TextStyle(
                    color = if (focused) JpTvPalette.Ink else JpTvPalette.Paper,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }
    }
}

internal fun initials(label: String): String =
    label.filter { it.isLetter() }.take(2).uppercase()
