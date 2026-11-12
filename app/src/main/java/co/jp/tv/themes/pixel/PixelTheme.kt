package co.jp.tv.themes.pixel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation

object PixelTheme : LauncherTheme {
    override val id = "pixel"
    override val displayName = "Pixel"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val count = state.apps.size
        val focused = state.apps.getOrNull(state.focusedIndex)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pixelBackdrop()
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                    toThemes = { it == Key.DirectionRight },
                    fromThemes = { it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            PixelShapes()
            PixelGridFloor()
            PixelBrand(
                state.brand,
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 40.dp, top = 24.dp),
            )
            PixelTopBar(
                state.network,
                state.now,
                state.config.hourFormat,
                actions,
                focus,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 40.dp, top = 22.dp),
                weather = state.weather,
            )
            PixelShelf(
                state.apps,
                state.focusedIndex,
                Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 95.dp),
            )
            if (count > 0) {
                PixelArrow(
                    PixelIconKind.ChevronLeft,
                    Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 220.dp),
                    onClick = { actions.focus((state.focusedIndex - 1).coerceIn(0, count - 1)) },
                )
                PixelArrow(
                    PixelIconKind.ChevronRight,
                    Modifier.align(Alignment.TopEnd).padding(end = 32.dp, top = 220.dp),
                    onClick = { actions.focus((state.focusedIndex + 1).coerceIn(0, count - 1)) },
                )
            }
            if (focused != null) {
                PixelSelection(
                    name = focused.label,
                    glow = PixelPalette.Tints[state.focusedIndex % PixelPalette.Tints.size].top,
                    position = "%02d / %02d".format(state.focusedIndex + 1, count),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                )
            }
        }
    }
}

/** J-P-dash-... in the console's four colors, one per character. */
@Composable
private fun PixelBrand(brand: String, modifier: Modifier = Modifier) {
    val letters = listOf(PixelPalette.Red, PixelPalette.Green, PixelPalette.White, PixelPalette.Gold, PixelPalette.Blue)
    val text: AnnotatedString = remember(brand) {
        buildAnnotatedString {
            brand.forEachIndexed { i, c ->
                withStyle(SpanStyle(color = letters[i % letters.size])) { append(c) }
            }
        }
    }
    BasicText(
        text,
        modifier = modifier.pixelEntrance(0),
        style = TextStyle(
            fontSize = 30.sp,
            lineHeight = 1.em,
            letterSpacing = 0.02.em,
            fontFamily = JpFonts.Fredoka,
            shadow = rememberPixelShadow(PixelPalette.ShadowBrand, 2.5f),
        ),
    )
}

@Composable
private fun PixelArrow(kind: PixelIconKind, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(36.dp)
            .drawBehind {
                // box-shadow: 0 6px 0 — a hard edge under the button.
                drawCircle(PixelPalette.GoldDark, radius = size.minDimension / 2f, center = center + Offset(0f, 3.dp.toPx()))
            }
            .background(PixelPalette.Gold, CircleShape)
            .pixelTap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        PixelIcon(kind, PixelPalette.Ink, 3.4f, 15.dp)
    }
}

/** The focused app's name with a glow in its cartridge color, and its position in the list. */
@Composable
private fun PixelSelection(name: String, glow: androidx.compose.ui.graphics.Color, position: String, modifier: Modifier) {
    val density = LocalDensity.current
    val glowShadow = remember(glow, density) { Shadow(glow.copy(alpha = 0.6f), Offset.Zero, with(density) { 22.dp.toPx() }) }
    val nameShadow = rememberPixelShadow(PixelPalette.ShadowName, 3f)
    val positionShadow = rememberPixelShadow(PixelPalette.ShadowBrand, 1.5f)
    val nameStyle = TextStyle(
        color = PixelPalette.White,
        fontSize = 38.sp,
        lineHeight = 1.em,
        letterSpacing = 0.01.em,
        fontFamily = JpFonts.Fredoka,
        textAlign = TextAlign.Center,
    )
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        // text-shadow takes two layers in the design; Compose takes one per text, so the glow is a copy underneath.
        Box(contentAlignment = Alignment.Center) {
            BasicText(name, style = nameStyle.copy(shadow = glowShadow), maxLines = 1)
            BasicText(name, style = nameStyle.copy(shadow = nameShadow), maxLines = 1)
        }
        BasicText(
            position,
            style = TextStyle(
                color = PixelPalette.SubtleWhite,
                fontSize = 12.sp,
                letterSpacing = 0.16.em,
                fontFamily = JpFonts.Fredoka,
                shadow = positionShadow,
            ),
        )
    }
}

/** Vertical purple gradient plus the boot-screen glow (radial-gradient 60% 50% at 50% 38%). */
private fun Modifier.pixelBackdrop(): Modifier = drawWithCache {
    val sky = Brush.verticalGradient(
        0f to PixelPalette.BackgroundTop,
        0.52f to PixelPalette.BackgroundMid,
        1f to PixelPalette.BackgroundBottom,
    )
    val ry = size.height * 0.5f
    val sx = (size.width * 0.6f) / ry
    val center = Offset(size.width * 0.5f, size.height * 0.38f)
    val glow = Brush.radialGradient(
        0f to PixelPalette.GlowCore,
        0.7f to PixelPalette.GlowClear,
        center = center,
        radius = ry,
    )
    onDrawBehind {
        drawRect(sky)
        scale(sx, 1f, pivot = center) { drawRect(glow, size = size) }
    }
}
