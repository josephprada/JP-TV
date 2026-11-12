package co.jp.tv.themes.vinilo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.bakeToBitmap
import kotlin.math.ceil
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.launchOrigin
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.networkName
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.RailWindow
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themesControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation
import co.jp.tv.core.util.rememberLowEnd
import co.jp.tv.wallpaper.cssGradientLine
import java.time.LocalDateTime
import kotlinx.coroutines.delay

// The design is 1920x1080; dp and sp are half of its px.

/**
 * The shelf shows this many sleeves. The artboard has five, but with the focused sleeve open (210 dp
 * plus 85 dp each and 11 dp gaps) five run to x = 634 dp, under the turntable that starts at 570 dp.
 * Four end at 538 dp, inside the 555 dp shelf board.
 */
private const val ShelfSlots = 4

private val SleeveShape = RoundedCornerShape(5.dp)

private class EqBar(val halfCycleMs: Int, val delayMs: Int)

private val EqBars = listOf(EqBar(450, 0), EqBar(350, 200), EqBar(550, 100), EqBar(400, 300))

object ViniloTheme : LauncherTheme {
    override val id = "vinilo"
    override val displayName = "Vinilo"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val deck = rememberDeckMotion()
        val selected = state.apps.getOrNull(state.focusedIndex)
        val labelColor = ViniloPalette.LabelColors[state.focusedIndex.coerceAtLeast(0) % ViniloPalette.LabelColors.size]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .viniloBackdrop()
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
            ViniloHeader(
                brand = state.brand,
                network = state.network,
                now = state.now,
                hourFormat = state.config.hourFormat,
                actions = actions,
                focus = focus,
                modifier = Modifier.align(Alignment.TopStart),
            )
            ViniloTurntable(
                deck = deck,
                labelName = selected?.label.orEmpty(),
                labelColor = labelColor,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 85.dp),
            )
            ViniloNowPlaying(
                selected = selected?.label.orEmpty(),
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 380.dp),
            )
            ViniloShelfBoard()
            ViniloShelf(
                apps = state.apps,
                focusedIndex = state.focusedIndex,
                spin = deck.spin,
                modifier = Modifier.align(Alignment.TopStart).offset(x = 40.dp, y = 150.dp),
            )
            BasicText(
                stringResource(R.string.vinilo_side, state.apps.size),
                modifier = Modifier.align(Alignment.TopStart).offset(x = 40.dp, y = 385.dp),
                style = TextStyle(
                    color = ViniloPalette.CreamDim,
                    fontSize = 7.5.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.2.em,
                ),
            )
        }
    }
}

/** radial-gradient(90% 80% at 30% 20%, deck 0, background 60%, edge 100%): a circle scaled into an ellipse. */
private fun Modifier.viniloBackdrop(): Modifier = drawWithCache {
    val center = Offset(size.width * 0.3f, size.height * 0.2f)
    val radiusX = size.width * 0.9f
    val radiusY = size.height * 0.8f
    val glow = Brush.radialGradient(
        0f to ViniloPalette.BackgroundGlow,
        0.6f to ViniloPalette.Background,
        1f to ViniloPalette.BackgroundEdge,
        center = center,
        radius = radiusX,
    )
    val reach = size.width * 4f
    onDrawBehind {
        scale(1f, radiusY / radiusX, pivot = center) {
            drawRect(
                brush = glow,
                topLeft = Offset(center.x - reach, center.y - reach),
                size = Size(reach * 2f, reach * 2f),
            )
        }
    }
}

/** Everything that loops on this theme. While paused no transition is created, so it all rests. */
private class DeckMotion(
    /** Record rotation, degrees. */
    val spin: () -> Float,
    /** Tonearm angle, degrees. */
    val arm: () -> Float,
    /** Equalizer bar heights, 0.3..1, in [EqBars] order. */
    val eq: List<() -> Float>,
)

private val StillDeck = DeckMotion(
    spin = { 0f },
    arm = { 22f },
    eq = EqBars.map { { 0.3f } },
)

@Composable
private fun rememberDeckMotion(): DeckMotion {
    if (LocalPaused.current) return StillDeck
    val transition = rememberInfiniteTransition(label = "vinDeck")
    val spin = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    val arm = transition.animateFloat(
        initialValue = 22f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(1500, easing = JpMotion.InOut), RepeatMode.Reverse),
        label = "arm",
    )
    val eq = EqBars.map { bar ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(bar.halfCycleMs, easing = JpMotion.InOut),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(bar.delayMs),
            ),
            label = "eq",
        )
    }
    return remember(transition) {
        DeckMotion(
            spin = { spin.value },
            arm = { arm.value },
            eq = eq.map { bar -> { bar.value } },
        )
    }
}

@Composable
private fun ViniloHeader(
    brand: String,
    network: NetworkStatus,
    now: LocalDateTime,
    hourFormat: ClockFormat.HourFormat,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val records = stringResource(R.string.vinilo_records)
    val title = remember(brand, records) {
        buildAnnotatedString {
            append("$brand ")
            withStyle(SpanStyle(color = ViniloPalette.Gold)) { append(records) }
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            title,
            style = TextStyle(color = ViniloPalette.Orange, fontSize = 28.sp, fontFamily = JpFonts.Righteous),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val ms = network.routerLatencyMs ?: network.internetLatencyMs
            BasicText(
                "${networkName(network).uppercase()}${ms?.let { " · ${it} MS" }.orEmpty()}",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                ),
                style = TextStyle(color = ViniloPalette.Cream, fontSize = 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.16.em),
            )
            BasicText(
                ClockFormat.time(now, hourFormat),
                style = TextStyle(color = ViniloPalette.Cream, fontSize = 22.sp, fontFamily = JpFonts.Righteous),
            )
            HeaderPill(
                text = stringResource(R.string.cd_settings).uppercase(),
                focused = focus.onControl,
                onClick = actions::openSettings,
                modifier = Modifier.themeControlTarget(focus),
            )
            // Not in the artboard: a second pill so the Themes screen is one D-pad step from Settings.
            HeaderPill(
                text = stringResource(R.string.cd_themes).uppercase(),
                focused = focus.onThemes,
                onClick = actions::openThemes,
                modifier = Modifier.themesControlTarget(focus),
            )
        }
    }
}

/** Gold outlined pill in the header; [focused] thickens the outline and tints the fill. */
@Composable
private fun HeaderPill(text: String, focused: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BasicText(
        text,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (focused) ViniloPalette.Gold.copy(alpha = 0.25f) else Color.Transparent)
            .border(if (focused) 2.dp else 1.dp, ViniloPalette.Gold, RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 11.dp, vertical = 6.dp),
        style = TextStyle(color = ViniloPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.16.em),
    )
}

/** Wooden shelf under the sleeves. */
@Composable
private fun ViniloShelfBoard() {
    val shape = RoundedCornerShape(5.dp)
    Box(
        Modifier
            .offset(x = 25.dp, y = 345.dp)
            .size(530.dp, 20.dp)
            .shadow(10.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    0f to ViniloPalette.ShelfTop,
                    0.6f to ViniloPalette.ShelfMid,
                    1f to ViniloPalette.ShelfBottom,
                ),
            ),
    )
}

@Composable
private fun ViniloShelf(
    apps: List<TvApp>,
    focusedIndex: Int,
    spin: () -> Float,
    modifier: Modifier = Modifier,
) {
    // Sleeves rise in only on the first pass; sleeves revealed by moving the focus appear at once.
    var firstPass by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1_500)
        firstPass = false
    }
    var previousTop by remember { mutableStateOf(0) }
    val top = RailWindow.top(previousTop, focusedIndex, apps.size, ShelfSlots)
    SideEffect { previousTop = top }
    val visible = apps.size.coerceAtMost(top + ShelfSlots) - top
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        for (slot in 0 until visible) {
            val index = top + slot
            val app = apps[index]
            key(app.packageName) {
                ViniloCover(
                    app = app,
                    focused = index == focusedIndex,
                    color = ViniloPalette.LabelColors[index % ViniloPalette.LabelColors.size],
                    slot = slot,
                    rise = firstPass,
                    spin = spin,
                )
            }
        }
    }
}

/** An LP sleeve; the focused one grows and slides its record out to the right. */
@Composable
private fun ViniloCover(app: TvApp, focused: Boolean, color: Color, slot: Int, rise: Boolean, spin: () -> Float) {
    val spec = tween<Dp>(JpMotion.NavMs, easing = JpMotion.Entrance)
    val sleeve by animateDpAsState(if (focused) 150.dp else 85.dp, spec, label = "sleeve")
    val width by animateDpAsState(if (focused) 210.dp else 85.dp, spec, label = "coverWidth")
    val discLeft by animateDpAsState(if (focused) 65.dp else 10.dp, spec, label = "discLeft")
    val discTop = if (focused) 5.dp else 3.dp
    val lowEnd = rememberLowEnd()
    val entrance = remember { Animatable(if (rise) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!rise) return@LaunchedEffect
        delay(slot * 80L)
        entrance.animateTo(1f, tween(JpMotion.RiseMs, easing = JpMotion.Entrance))
    }
    Box(
        Modifier
            .width(width)
            .size(width, sleeve)
            .graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 20.dp.toPx()
            },
    ) {
        Box(
            Modifier
                .offset(x = discLeft, y = discTop)
                .size(sleeve - 10.dp)
                .graphicsLayer { rotationZ = if (focused) spin() else 0f }
                .clip(CircleShape)
                .vinylGrooves(sheen = false),
        )
        Box(
            Modifier
                .size(sleeve)
                .launchOrigin(focused)
                .softShadow(lowEnd, 8.dp, SleeveShape)
                .clip(SleeveShape)
                .background(color)
                .focusRing(focused),
            contentAlignment = Alignment.BottomStart,
        ) {
            BasicText(
                app.label,
                modifier = Modifier.padding(9.dp),
                style = TextStyle(
                    color = ViniloPalette.Background,
                    fontSize = if (focused) 19.sp else 11.sp,
                    lineHeight = if (focused) 20.sp else 12.sp,
                    fontFamily = JpFonts.Righteous,
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Elevation shadows are redrawn by the GPU every frame the deck spins; low-end boxes go without the small ones. */
private fun Modifier.softShadow(lowEnd: Boolean, elevation: Dp, shape: Shape): Modifier =
    if (lowEnd) this else shadow(elevation, shape)

/** 2 dp cream outline drawn 2 dp outside the sleeve. */
private fun Modifier.focusRing(focused: Boolean): Modifier = if (!focused) this else drawWithCache {
    val ring = Stroke(width = 2.dp.toPx())
    val outset = 3.dp.toPx()
    val corner = CornerRadius(8.dp.toPx())
    onDrawWithContent {
        drawContent()
        drawRoundRect(
            color = ViniloPalette.Cream,
            topLeft = Offset(-outset, -outset),
            size = Size(size.width + outset * 2f, size.height + outset * 2f),
            cornerRadius = corner,
            style = ring,
        )
    }
}

/** Black vinyl: concentric grooves, plus an optional soft sheen near the top. */
private fun Modifier.vinylGrooves(sheen: Boolean): Modifier = drawWithCache {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f
    val pitch = 2.5.dp.toPx()
    val line = Stroke(width = 0.75.dp.toPx())
    val glint = Brush.radialGradient(
        colors = listOf(ViniloPalette.Sheen, ViniloPalette.SheenClear),
        center = Offset(size.width * 0.5f, size.height * 0.23f),
        radius = size.width * 0.2f,
    )
    val glintTopLeft = Offset(size.width * 0.3f, size.height * 0.08f)
    val glintSize = Size(size.width * 0.4f, size.height * 0.3f)
    // Dozens of concentric rings: drawn once into a bitmap, so a spinning record only rotates a texture.
    val grooves = bakeToBitmap(ceil(size.width).toInt(), ceil(size.height).toInt()) {
        drawCircle(ViniloPalette.Groove, radius = radius, center = center)
        var ring = pitch * 0.8f
        while (ring < radius) {
            drawCircle(ViniloPalette.GrooveLine, radius = ring, center = center, style = line)
            ring += pitch
        }
        if (sheen) drawOval(glint, topLeft = glintTopLeft, size = glintSize)
    }
    onDrawBehind { drawImage(grooves) }
}

@Composable
private fun ViniloTurntable(
    deck: DeckMotion,
    labelName: String,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    val plinth = RoundedCornerShape(18.dp)
    Box(
        modifier
            .size(350.dp, 280.dp)
            .shadow(16.dp, plinth)
            .clip(plinth)
            .drawWithCache {
                val (start, end) = cssGradientLine(160f, size.width, size.height)
                val wood = Brush.linearGradient(
                    colors = listOf(ViniloPalette.DeckTop, ViniloPalette.Deck),
                    start = start,
                    end = end,
                )
                onDrawBehind { drawRect(wood) }
            },
    ) {
        // Platter and record.
        Box(
            Modifier
                .offset(x = 25.dp, y = 20.dp)
                .size(240.dp)
                .softShadow(rememberLowEnd(), 6.dp, CircleShape)
                .clip(CircleShape)
                .background(ViniloPalette.Vinyl),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(226.dp)
                    .graphicsLayer { rotationZ = deck.spin() }
                    .clip(CircleShape)
                    .vinylGrooves(sheen = true),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(labelColor),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        labelName,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = TextStyle(
                            color = ViniloPalette.Background,
                            fontSize = 10.sp,
                            fontFamily = JpFonts.Righteous,
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(Modifier.size(6.dp).clip(CircleShape).background(ViniloPalette.Background))
                }
            }
        }
        ViniloTonearm(deck.arm, Modifier.offset(x = 285.dp, y = 30.dp))
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = 280.dp, y = (-20).dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            ViniloPalette.EqColors.forEachIndexed { i, tint ->
                val level = deck.eq[i]
                Box(
                    Modifier
                        .size(5.dp, 30.dp)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            scaleY = level()
                        }
                        .clip(RoundedCornerShape(2.dp))
                        .background(tint),
                )
            }
        }
    }
}

/** Pivot cap, rod and cartridge, swaying around the pivot. */
@Composable
private fun ViniloTonearm(angle: () -> Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(10.dp, 168.dp)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 5f / 168f)
                rotationZ = angle()
            }
            .drawWithCache {
                val dp = 1.dp.toPx()
                onDrawBehind {
                    drawCircle(ViniloPalette.ArmMetal, radius = 5f * dp, center = Offset(5f * dp, 5f * dp))
                    drawRoundRect(
                        color = ViniloPalette.ArmRod,
                        topLeft = Offset(3.5f * dp, 5f * dp),
                        size = Size(3f * dp, 150f * dp),
                        cornerRadius = CornerRadius(1.5f * dp),
                    )
                    drawRoundRect(
                        color = ViniloPalette.ArmMetal,
                        topLeft = Offset(-2f * dp, 150f * dp),
                        size = Size(14f * dp, 18f * dp),
                        cornerRadius = CornerRadius(3f * dp),
                    )
                }
            },
    )
}

@Composable
private fun ViniloNowPlaying(selected: String, modifier: Modifier = Modifier) {
    Column(modifier.width(350.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        BasicText(
            stringResource(R.string.vinilo_now_playing),
            style = TextStyle(
                color = ViniloPalette.Gold,
                fontSize = 7.5.sp,
                fontFamily = JpFonts.Mono,
                letterSpacing = 0.2.em,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        BasicText(
            selected,
            style = TextStyle(color = ViniloPalette.Cream, fontSize = 20.sp, fontFamily = JpFonts.Righteous),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
