package co.jp.tv.themes.jptv

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.riseIn
import co.jp.tv.themes.shared.HomeActions

@Composable
internal fun TopPills(accent: Color, actions: HomeActions, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(top = 20.dp, end = 20.dp)
            .riseIn(delayMs = 200, durationMs = JpMotion.FadeMs, distanceDp = 12f),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NetworkChip(accent, onClick = actions::openNetwork)
        RoundButton(JpIcons.Settings, stringResource(R.string.cd_settings), accent, onClick = actions::openSettings)
        RoundButton(JpIcons.Palette, stringResource(R.string.cd_themes), accent, onClick = actions::openThemes)
    }
}

/** Glass pill that shows an accent ring and lifts slightly while focused. */
@Composable
private fun GlassPill(
    description: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val p by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "pill")
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = 1f + 0.05f * p; scaleY = 1f + 0.05f * p }
            .glass(JpDimens.PillShape)
            .border(JpDimens.Hairline, accent.copy(alpha = 0.7f * p), JpDimens.PillShape)
            .semantics { contentDescription = description }
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun NetworkChip(accent: Color, onClick: () -> Unit) {
    // Placeholder content: live data arrives in phase 3.
    GlassPill(stringResource(R.string.cd_network), accent, onClick, Modifier.height(40.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            JpIconView(JpIcons.Wifi, 13.dp, JpColors.Paper)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                BasicText(
                    stringResource(R.string.network_chip_title),
                    style = TextStyle(JpColors.Paper, 9.5.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
                )
                BasicText(
                    stringResource(R.string.network_chip_subtitle),
                    style = TextStyle(JpColors.Muted, 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                )
            }
            SignalBars(filled = 3)
        }
    }
}

@Composable
private fun SignalBars(filled: Int) {
    Row(Modifier.height(11.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(3.5f, 6f, 8.5f, 11f).forEachIndexed { i, h ->
            Box(
                Modifier
                    .width(2.5.dp)
                    .height(h.dp)
                    .graphicsLayer { alpha = if (i < filled) 1f else 0.25f }
                    .background(JpColors.Paper, RoundedCornerShape(1.5.dp)),
            )
        }
    }
}

@Composable
private fun RoundButton(icon: JpIcon, description: String, accent: Color, onClick: () -> Unit) {
    GlassPill(description, accent, onClick, Modifier.size(40.dp)) {
        JpIconView(icon, 14.dp, JpColors.Paper)
    }
}
