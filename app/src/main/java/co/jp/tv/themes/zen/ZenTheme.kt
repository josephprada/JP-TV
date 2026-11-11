package co.jp.tv.themes.zen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.jptv.JpIcon
import co.jp.tv.themes.jptv.JpIconView
import co.jp.tv.themes.jptv.JpIcons
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation
import co.jp.tv.wallpaper.cssGradientLine
import java.time.LocalDateTime
import kotlin.math.abs

// The design is 1920x1080; dp and sp are half of its px. The wallpaper is the app-wide one
// (MainActivity draws it behind every theme), so this theme only adds a scrim on top.

private const val SmallRowDp = 38f
private const val BigRowDp = 72f
private const val EaseMs = 600
private const val RiseMs = 800
private val ZenEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private val IconShape = RoundedCornerShape(percent = 23)

/** Geist at weight 300, which [JpFonts.Body] does not register. */
@OptIn(ExperimentalTextApi::class)
private object ZenFonts {
    val Light = FontFamily(
        Font(
            resId = R.font.geist,
            weight = FontWeight.Light,
            variationSettings = FontVariation.Settings(FontVariation.weight(300)),
        ),
    )
}

object ZenTheme : LauncherTheme {
    override val id = "zen"
    override val displayName = "Zen"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionRight || (it == Key.DirectionUp && state.focusedIndex <= 0) },
                    toList = { it == Key.DirectionDown || it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Vertical),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to ZenPalette.ScrimStrong,
                            0.45f to ZenPalette.ScrimMid,
                            0.75f to ZenPalette.ScrimClear,
                        ),
                    ),
            )
            ZenAppList(state.apps, state.focusedIndex)
            ZenBrand(state.brand, Modifier.align(Alignment.TopStart).padding(start = 70.dp, top = 32.dp))
            ZenTopRight(
                now = state.now,
                hourFormat = state.config.hourFormat,
                actions = actions,
                focus = focus,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 48.dp, top = 24.dp),
            )
            BasicText(
                "%02d / %02d".format(state.focusedIndex + 1, state.apps.size),
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 70.dp, bottom = 28.dp),
                style = TextStyle(
                    color = ZenPalette.PaperFaint,
                    fontSize = 7.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.2.em,
                ),
            )
        }
    }
}

/** Fades in and rises 8 dp once, after [delayMs]. */
@Composable
private fun rememberRise(delayMs: Int): State<Float> {
    val rise = remember { Animatable(0f) }
    LaunchedEffect(Unit) { rise.animateTo(1f, tween(RiseMs, delayMillis = delayMs, easing = ZenEase)) }
    return rise.asState()
}

@Composable
private fun ZenBrand(brand: String, modifier: Modifier = Modifier) {
    val rise = rememberRise(0)
    BasicText(
        brand,
        modifier = modifier.graphicsLayer {
            alpha = rise.value
            translationY = (1f - rise.value) * 8.dp.toPx()
        },
        style = TextStyle(
            color = ZenPalette.Paper,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = JpFonts.Body,
            letterSpacing = (-0.02).em,
        ),
    )
}

/** Accordion list: the selected app big, the rest small, with the selection kept at mid-height. */
@Composable
private fun ZenAppList(apps: List<TvApp>, focusedIndex: Int) {
    val centre = 270f - (focusedIndex * SmallRowDp + BigRowDp / 2f)
    val offsetY by animateDpAsState(centre.dp, tween(EaseMs, easing = ZenEase), label = "zenList")
    Box(
        Modifier
            .width(600.dp)
            .fillMaxHeight()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val mask = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.2f to Color.Black,
                    0.8f to Color.Black,
                    1f to Color.Transparent,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(mask, blendMode = BlendMode.DstIn)
                }
            },
    ) {
        Column(
            Modifier
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .offset(y = offsetY),
        ) {
            apps.forEachIndexed { index, app ->
                key(app.packageName) { ZenRow(app, index, focusedIndex) }
            }
        }
    }
}

@Composable
private fun ZenRow(app: TvApp, index: Int, focusedIndex: Int) {
    val away = abs(index - focusedIndex)
    val focused = away == 0
    val ease = tween<Float>(EaseMs, easing = ZenEase)
    val grow by animateFloatAsState(if (focused) 1f else 0f, ease, label = "zenGrow")
    val fade by animateFloatAsState(
        when (away) {
            0 -> 1f
            1 -> 0.66f
            2 -> 0.54f
            else -> 0.46f
        },
        ease,
        label = "zenFade",
    )
    val tint = ZenPalette.Tints[index % ZenPalette.Tints.size]
    val glow = remember(tint) { Brush.radialGradient(listOf(tint.glow, tint.glowClear)) }
    val initials = remember(app.label) {
        app.label.filter { it in 'A'..'Z' || it in 'a'..'z' }.take(2).uppercase()
    }
    val iconSize = lerp(24f, 56f, grow).dp
    Row(
        Modifier
            .width(500.dp)
            .height(lerp(SmallRowDp, BigRowDp, grow).dp)
            .graphicsLayer { alpha = fade }
            .padding(start = 70.dp),
        horizontalArrangement = Arrangement.spacedBy(lerp(12f, 16f, grow).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(iconSize)
                .drawBehindGlow(glow, grow)
                .clip(IconShape)
                .drawWithCache {
                    val (start, end) = cssGradientLine(160f, size.width, size.height)
                    val paint = Brush.linearGradient(listOf(tint.color, tint.fade), start = start, end = end)
                    onDrawBehind { drawRect(paint) }
                }
                .border(0.5.dp, if (focused) ZenPalette.RingStrong else ZenPalette.RingSoft, IconShape),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                initials,
                style = TextStyle(
                    color = tint.ink,
                    fontSize = lerp(9f, 20f, grow).sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = JpFonts.Body,
                    letterSpacing = (-0.02).em,
                ),
            )
        }
        val nameSize = lerp(16f, 28f, grow)
        BasicText(
            app.label,
            style = TextStyle(
                color = ZenPalette.Paper,
                fontSize = nameSize.sp,
                lineHeight = (nameSize * 1.05f).sp,
                fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = JpFonts.Body,
                letterSpacing = lerp(-0.015f, -0.035f, grow).em,
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Soft colored glow below the icon, `0 24px 60px color66` in the design, scaled by [amount]. */
private fun Modifier.drawBehindGlow(glow: Brush, amount: Float): Modifier = drawWithCache {
    val spread = 30.dp.toPx()
    val drop = 12.dp.toPx()
    onDrawBehind {
        if (amount > 0.01f) {
            translate(top = drop) {
                drawRect(
                    brush = glow,
                    topLeft = Offset(-spread, -spread),
                    size = Size(size.width + spread * 2f, size.height + spread * 2f),
                    alpha = amount,
                )
            }
        }
    }
}

@Composable
private fun ZenTopRight(
    now: LocalDateTime,
    hourFormat: ClockFormat.HourFormat,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val rise = rememberRise(150)
    val networkLabel = stringResource(R.string.cd_network)
    Row(
        modifier.graphicsLayer {
            alpha = rise.value
            translationY = (1f - rise.value) * 8.dp.toPx()
        },
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .height(24.dp)
                .semantics { contentDescription = networkLabel }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JpIconView(JpIcons.Wifi, 13.dp, ZenPalette.PaperSoft)
            BasicText(
                "[RED WI-FI]",
                style = TextStyle(color = ZenPalette.PaperSoft, fontSize = 10.sp, fontFamily = JpFonts.Body),
            )
        }
        ZenIconButton(
            icon = JpIcons.Settings,
            description = stringResource(R.string.cd_settings),
            highlighted = focus.onControl,
            onClick = actions::openSettings,
            modifier = Modifier.themeControlTarget(focus),
        )
        ZenIconButton(
            icon = JpIcons.Palette,
            description = stringResource(R.string.cd_themes),
            highlighted = false,
            onClick = actions::openThemes,
        )
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            BasicText(
                ClockFormat.time(now, hourFormat),
                style = TextStyle(
                    color = ZenPalette.Paper,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    fontFamily = ZenFonts.Light,
                    letterSpacing = (-0.02).em,
                ),
            )
            BasicText(
                ClockFormat.date(now),
                style = TextStyle(
                    color = ZenPalette.PaperMuted,
                    fontSize = 6.5.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.2.em,
                ),
            )
        }
    }
}

/** Borderless 24 dp icon target; [highlighted] shows the white focus ring 2 dp outside it. */
@Composable
private fun ZenIconButton(
    icon: JpIcon,
    description: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(24.dp)
            .semantics { contentDescription = description }
            .drawWithCache {
                val ring = Stroke(width = 1.dp.toPx())
                val outset = 2.dp.toPx()
                val corner = CornerRadius(6.dp.toPx())
                onDrawBehind {
                    if (highlighted) {
                        drawRoundRect(
                            color = ZenPalette.FocusRing,
                            topLeft = Offset(-outset, -outset),
                            size = Size(size.width + outset * 2f, size.height + outset * 2f),
                            cornerRadius = corner,
                            style = ring,
                        )
                    }
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        JpIconView(icon, 14.dp, if (highlighted) ZenPalette.Paper else ZenPalette.PaperSoft)
    }
}
