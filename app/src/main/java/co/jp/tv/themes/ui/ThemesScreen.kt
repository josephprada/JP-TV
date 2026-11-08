package co.jp.tv.themes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.riseIn
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.ThemeRegistry

private const val COLUMNS = 4

/** Theme picker grid matching Themes.dc.html. */
@Composable
fun ThemesScreen(
    activeThemeId: String,
    accent: Color,
    onSelect: (String) -> Unit,
    onCustomize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themes = ThemeRegistry.all
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
    val activeName = ThemeRegistry.byId(activeThemeId).displayName

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JpColors.Ink)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                BasicText(
                    text = stringResource(R.string.themes_title),
                    style = TextStyle(color = JpColors.Paper, fontSize = 20.sp, fontFamily = JpFonts.Body),
                )
                BasicText(
                    text = stringResource(R.string.themes_breadcrumb, themes.size, activeName.uppercase()),
                    style = TextStyle(color = JpColors.Faint, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                )
            }
            CustomizeLink(accent = accent, onClick = onCustomize)
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            themes.chunked(COLUMNS).forEachIndexed { row, rowThemes ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowThemes.forEachIndexed { col, theme ->
                        val index = row * COLUMNS + col
                        ThemeCard(
                            theme = theme,
                            active = theme.id == activeThemeId,
                            accent = accent,
                            delayMs = 80 + index * 40,
                            onClick = { onSelect(theme.id) },
                            modifier = Modifier
                                .weight(1f)
                                .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier),
                        )
                    }
                    repeat(COLUMNS - rowThemes.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CustomizeLink(accent: Color, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    BasicText(
        text = stringResource(R.string.themes_customize),
        style = TextStyle(color = JpColors.Paper, fontSize = 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        modifier = Modifier
            .background(JpColors.Paper.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .border(1.dp, if (focused) accent else JpColors.Paper.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}

@Composable
private fun ThemeCard(
    theme: LauncherTheme,
    active: Boolean,
    accent: Color,
    delayMs: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val ring = if (active || focused) accent.copy(alpha = 0.7f) else JpColors.Paper.copy(alpha = 0.12f)
    Column(
        modifier = modifier
            .riseIn(delayMs = delayMs)
            .background(JpColors.Paper.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
            .border(1.dp, ring, RoundedCornerShape(18.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0A0A0C), Color(0xFF140A0B))),
                    RoundedCornerShape(11.dp),
                ),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(theme.displayName, style = TextStyle(color = JpColors.Paper, fontSize = 13.sp, fontFamily = JpFonts.Body))
            BasicText(
                text = if (active) stringResource(R.string.themes_active) else stringResource(R.string.themes_use),
                style = TextStyle(
                    color = if (active) accent else JpColors.Faint,
                    fontSize = 6.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.12.em,
                ),
            )
        }
        BasicText(
            text = themeBlurb(theme.id),
            style = TextStyle(color = JpColors.Muted, fontSize = 8.sp, fontFamily = JpFonts.Body, lineHeight = 11.sp),
        )
    }
}

private fun themeBlurb(id: String): String = when (id) {
    "jptv" -> "Vidrio oscuro, galaxia o brasas. Colores y fuente a tu gusto."
    "pixel" -> "Carrusel pixel-art y tipografía de consola."
    "terminal" -> "Lista estilo CLI, verde sobre negro."
    "cartelera" -> "Pósters apaisados tipo cine."
    "zen" -> "Poco UI, menú que aparece al enfocar."
    "hud" -> "Rueda táctica y datos en overlay."
    "vinilo" -> "Discos y tipografía de tienda de records."
    "lofi" -> "Ambiente cálido y relajado."
    else -> ""
}
