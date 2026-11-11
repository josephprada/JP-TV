package co.jp.tv.themes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ThemeRegistry

private const val COLUMNS = 4

/**
 * Theme picker matching Themes.dc.html: a 1920x1340 artboard, taller than the screen, so the page scrolls
 * (focus moves bring the focused card into view). One card per registered theme, four per row.
 */
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                BasicText(
                    text = stringResource(R.string.themes_title),
                    modifier = Modifier.alignByBaseline(),
                    style = TextStyle(color = JpColors.Paper, fontSize = 20.sp, fontWeight = FontWeight.Medium, fontFamily = JpFonts.Body),
                )
                BasicText(
                    text = stringResource(R.string.themes_breadcrumb, themes.size, activeName.uppercase()),
                    modifier = Modifier.alignByBaseline(),
                    style = TextStyle(color = JpColors.Faint, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                )
            }
            CustomizeLink(accent = accent, onClick = onCustomize)
        }

        // Rows stretch to the artboard's 371 px (185.5 dp) when their content is shorter.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            themes.chunked(COLUMNS).forEachIndexed { row, rowThemes ->
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 186.dp).height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowThemes.forEachIndexed { col, theme ->
                        val index = row * COLUMNS + col
                        ThemeCard(
                            theme = theme,
                            active = theme.id == activeThemeId,
                            accent = accent,
                            delayMs = 80 + index * 50,
                            onClick = { onSelect(theme.id) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
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
            .border(0.5.dp, if (focused) accent else JpColors.Paper.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
