package co.jp.tv.themes.cartelera

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themesControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation
import java.time.LocalDateTime
import co.jp.tv.core.design.bakeToBitmap
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableIntStateOf
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign
import kotlin.math.sqrt

// The design is 1920x1080; dp and sp are half of its px.

private const val BulbCount = 30
private val BulbSize = 8.dp

/** Lamps and the focused frame blink on a 1.2 s cycle; three delays make the chase. */
private const val LampCycleMs = 1200f
private const val LampLoopMs = 3600

/** Every lamp edge falls on a multiple of this (half a cycle, three delays of a third of one). */
private const val LampStepMs = 200
private const val LampSteps = LampLoopMs / LampStepMs

/** Posters further than this many places from the focus are fully transparent, so they are not composed. */
private const val PosterWindow = 4
private const val CoverMs = JpMotion.NavMs
private val CoverEase = JpMotion.Entrance

/** CSS perspective of the coverflow stage, in design px. */
private const val PerspectivePx = 1800f

/** Vertical gap, in design px, between the poster centre and the perspective origin (50% vs 40% of 740). */
private const val PerspectiveOriginGapPx = 74f

private val FaceShape = RoundedCornerShape(8.dp)
private val StillClock: () -> Float = { 0f }

object CarteleraTheme : LauncherTheme {
    override val id = "cartelera"
    override val displayName = "Cartelera"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val clock = rememberLampClock()
        val selected = state.apps.getOrNull(state.focusedIndex)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CarteleraPalette.Background)
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionDown },
                    toList = { it == Key.DirectionUp },
                    toThemes = { it == Key.DirectionRight },
                    fromThemes = { it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            VelvetBackdrop()
            CarteleraStage(
                apps = state.apps,
                focusedIndex = state.focusedIndex,
                clock = clock,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 115.dp)
                    .fillMaxWidth()
                    .height(370.dp),
            )
            CarteleraMarquee(
                brand = state.brand,
                now = state.now,
                hourFormat = state.config.hourFormat,
                clock = clock,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 20.dp),
            )
            CarteleraArrow(
                forward = false,
                description = stringResource(R.string.cartelera_prev),
                onClick = { actions.focus(state.focusedIndex - 1) },
                modifier = Modifier.align(Alignment.TopStart).offset(x = 32.dp, y = 280.dp),
            )
            CarteleraArrow(
                forward = true,
                description = stringResource(R.string.cartelera_next),
                onClick = { actions.focus(state.focusedIndex + 1) },
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-32).dp, y = 280.dp),
            )
            CarteleraFooter(
                network = state.network,
                nowShowing = selected?.label.orEmpty(),
                actions = actions,
                focus = focus,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

/**
 * Milliseconds on the lamp cycle, read lazily from draw scopes. The lamps are CSS `steps(1)`, so
 * they only change every [LampStepMs]; the clock steps at that rate instead of ticking every frame,
 * which keeps the posters and the marquee from being redrawn 30 times a second for nothing. While
 * paused no ticker runs and the lamps stay on their first frame.
 */
@Composable
private fun rememberLampClock(): () -> Float {
    if (LocalPaused.current) return StillClock
    val step = remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(LampStepMs.toLong())
            step.intValue = (step.intValue + 1) % LampSteps
        }
    }
    return remember(step) { { step.intValue * LampStepMs.toFloat() } }
}

/** CSS `steps(1)` lamp: dark for the first half of the cycle, lit for the second, after [delayMs]. */
private fun lampLit(t: Float, delayMs: Float): Boolean {
    val phase = ((t - delayMs) % LampCycleMs + LampCycleMs) % LampCycleMs
    return phase >= LampCycleMs / 2f
}

/** Fades in and rises 12 dp once, when the theme first shows. */
@Composable
private fun rememberEntrance(): State<Float> {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    return animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(JpMotion.RiseMs, easing = JpMotion.Entrance),
        label = "cartRise",
    )
}

@Composable
private fun VelvetBackdrop() {
    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val folds = Brush.horizontalGradient(
                    colors = listOf(CarteleraPalette.VelvetA, CarteleraPalette.VelvetB, CarteleraPalette.VelvetA),
                    startX = 0f,
                    endX = 60.dp.toPx(),
                    tileMode = TileMode.Repeated,
                )
                // radial-gradient(70% 60% at 50% 45%): a circle scaled into an ellipse.
                val center = Offset(size.width * 0.5f, size.height * 0.45f)
                val radiusX = size.width * 0.7f
                val radiusY = size.height * 0.6f
                val vignette = Brush.radialGradient(
                    colors = listOf(CarteleraPalette.VignetteClear, CarteleraPalette.VignetteEdge),
                    center = center,
                    radius = radiusX,
                )
                val reach = size.width * 4f
                onDrawBehind {
                    drawRect(folds)
                    scale(1f, radiusY / radiusX, pivot = center) {
                        drawRect(
                            brush = vignette,
                            topLeft = Offset(center.x - reach, center.y - reach),
                            size = Size(reach * 2f, reach * 2f),
                        )
                    }
                }
            },
    )
}

@Composable
private fun CarteleraMarquee(
    brand: String,
    now: LocalDateTime,
    hourFormat: ClockFormat.HourFormat,
    clock: () -> Float,
    modifier: Modifier = Modifier,
) {
    val entrance = rememberEntrance()
    val titleShadow = with(LocalDensity.current) { Shadow(CarteleraPalette.TitleGlow, blurRadius = 12.dp.toPx()) }
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp)
            .graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 12.dp.toPx()
            }
            .clip(shape)
            .background(CarteleraPalette.MarqueeBg)
            .border(1.5.dp, CarteleraPalette.Gold, shape)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BulbRow(clock)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicText(
                stringResource(R.string.cartelera_presents, brand),
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(
                    color = CarteleraPalette.Gold,
                    fontSize = 8.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.2.em,
                ),
            )
            BasicText(
                stringResource(R.string.cartelera_tonight),
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(
                    color = CarteleraPalette.BulbOn,
                    fontSize = 30.sp,
                    fontFamily = JpFonts.Limelight,
                    letterSpacing = 0.08.em,
                    shadow = titleShadow,
                ),
            )
            BasicText(
                stringResource(R.string.cartelera_showtime, ClockFormat.time(now, hourFormat)),
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(
                    color = CarteleraPalette.Gold,
                    fontSize = 8.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.2.em,
                ),
            )
        }
        BulbRow(clock)
    }
}

@Composable
private fun BulbRow(clock: () -> Float) {
    Canvas(Modifier.fillMaxWidth().height(BulbSize).graphicsLayer()) {
        val diameter = BulbSize.toPx()
        val radius = diameter / 2f
        val halo = radius + 6.dp.toPx()
        val step = (size.width - diameter) / (BulbCount - 1)
        val t = clock()
        for (i in 0 until BulbCount) {
            val center = Offset(radius + i * step, radius)
            if (lampLit(t, (i % 3) * 400f)) {
                drawCircle(CarteleraPalette.BulbHalo, radius = halo, center = center)
                drawCircle(CarteleraPalette.BulbOn, radius = radius, center = center)
            } else {
                drawCircle(CarteleraPalette.BulbOff, radius = radius, center = center)
            }
        }
    }
}

@Composable
private fun CarteleraStage(
    apps: List<TvApp>,
    focusedIndex: Int,
    clock: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        // Spotlight cone from the lamp above the centre poster.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-5).dp)
                .size(480.dp, 370.dp)
                .drawWithCache {
                    val cone = Path().apply {
                        moveTo(size.width * 0.42f, 0f)
                        lineTo(size.width * 0.58f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    val light = Brush.verticalGradient(
                        colors = listOf(CarteleraPalette.ConeTop, CarteleraPalette.ConeBottom),
                        startY = 0f,
                        endY = size.height,
                    )
                    onDrawBehind { drawPath(cone, light) }
                },
        )
        // Warm pool of light on the floor.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp)
                .size(410.dp, 45.dp)
                .drawWithCache {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f * SQRT2
                    val pool = Brush.radialGradient(
                        0f to CarteleraPalette.FloorGlow,
                        0.7f to CarteleraPalette.FloorGlowClear,
                        center = center,
                        radius = radius,
                    )
                    val squashed = size.height / size.width
                    onDrawBehind {
                        scale(1f, squashed, pivot = center) {
                            drawRect(
                                brush = pool,
                                topLeft = Offset(0f, center.y - size.width / 2f),
                                size = Size(size.width, size.width),
                            )
                        }
                    }
                },
        )
        apps.forEachIndexed { index, app ->
            if (abs(index - focusedIndex) <= PosterWindow) {
                key(app.packageName) {
                    CarteleraPoster(
                        app = app,
                        index = index,
                        focusedIndex = focusedIndex,
                        clock = clock,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

private val SQRT2 = sqrt(2f)

@Composable
private fun CarteleraPoster(
    app: TvApp,
    index: Int,
    focusedIndex: Int,
    clock: () -> Float,
    modifier: Modifier = Modifier,
) {
    val distance = index - focusedIndex
    val away = abs(distance)
    val focused = distance == 0
    val side = distance.sign.toFloat()
    val tint = CarteleraPalette.PosterTints[index % CarteleraPalette.PosterTints.size]

    // Targets in design px / degrees; each one eases on the same curve as the design.
    val spec = tween<Float>(CoverMs, easing = CoverEase)
    val x = animateFloatAsState(if (focused) 0f else side * (270f + (away - 1) * 120f), spec, label = "posterX")
    val z = animateFloatAsState(if (focused) 80f else -away * 150f, spec, label = "posterZ")
    val turn = animateFloatAsState(if (focused) 0f else -side * 52f, spec, label = "posterTurn")
    val scale = animateFloatAsState(if (focused) 1.12f else 1f, spec, label = "posterScale")
    val fade = animateFloatAsState(
        if (away > 3) 0f else if (focused) 1f else 1f - away * 0.16f,
        spec,
        label = "posterFade",
    )
    val dim = animateFloatAsState(if (focused) 1f else maxOf(0.42f, 0.7f - away * 0.08f), spec, label = "posterDim")
    // 0 on a side poster, 1 when focused; drives the lamp frame.
    val onAmount: () -> Float = { ((scale.value - 1f) / 0.12f).coerceIn(0f, 1f) }

    Box(
        modifier
            .zIndex(100f - away)
            .size(170.dp, 250.dp)
            .launchOrigin(focused)
            .graphicsLayer {
                // translateZ is emulated through the CSS perspective factor p / (p - z).
                val f = PerspectivePx / (PerspectivePx - z.value)
                val k = scale.value * f
                scaleX = k
                scaleY = k
                translationX = x.value * f * 0.5f * density
                translationY = PerspectiveOriginGapPx * (f - 1f) * 0.5f * density
                rotationY = turn.value
                alpha = fade.value
                cameraDistance = 14f * density
            },
    ) {
        PosterFrame(onAmount, clock, Modifier.matchParentSize())
        Column(
            Modifier
                .matchParentSize()
                .shadow(12.dp, FaceShape)
                .clip(FaceShape)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(Color.Black, alpha = 1f - dim.value)
                    }
                }
                .background(Brush.verticalGradient(listOf(CarteleraPalette.VelvetB, CarteleraPalette.FaceBottom)))
                .border(
                    if (focused) 1.5.dp else 1.dp,
                    if (focused) CarteleraPalette.BulbOn else CarteleraPalette.GoldSoft,
                    FaceShape,
                ),
        ) {
            PosterArt(app.label.take(1), tint)
            Column(
                Modifier.padding(start = 12.dp, top = 9.dp, end = 12.dp, bottom = 11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                BasicText(
                    stringResource(R.string.cartelera_room, index + 1),
                    style = TextStyle(
                        color = CarteleraPalette.Gold,
                        fontSize = 6.5.sp,
                        fontFamily = JpFonts.Mono,
                        letterSpacing = 0.2.em,
                    ),
                )
                BasicText(
                    app.label,
                    style = TextStyle(
                        color = CarteleraPalette.Cream,
                        fontSize = 19.sp,
                        lineHeight = 20.sp,
                        fontFamily = JpFonts.Limelight,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Dotted marquee frame that grows out of the focused poster and blinks with the lamps. */
@Composable
private fun PosterFrame(onAmount: () -> Float, clock: () -> Float, modifier: Modifier = Modifier) {
    Box(
        modifier.graphicsLayer().drawWithCache {
            val stroke = 4.dp.toPx()
            val dots = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, stroke * 2f)),
            )
            val glow = Stroke(width = 14.dp.toPx())
            val corner = CornerRadius(14.dp.toPx())
            val reach = 13.dp.toPx()
            onDrawBehind {
                val amount = onAmount()
                if (amount > 0.01f) {
                    val grow = reach * amount - stroke / 2f
                    val topLeft = Offset(-grow, -grow)
                    val frame = Size(size.width + grow * 2f, size.height + grow * 2f)
                    drawRoundRect(
                        color = CarteleraPalette.FrameGlow,
                        topLeft = topLeft,
                        size = frame,
                        cornerRadius = corner,
                        style = glow,
                        alpha = amount,
                    )
                    drawRoundRect(
                        color = if (lampLit(clock(), 0f)) CarteleraPalette.FrameDim else CarteleraPalette.BulbOn,
                        topLeft = topLeft,
                        size = frame,
                        cornerRadius = corner,
                        style = dots,
                        alpha = amount,
                    )
                }
            }
        },
    )
}

/** Sunburst cover art with the app's initial. */
@Composable
private fun PosterArt(initial: String, tint: PosterTint) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clipToBounds()
            .drawWithCache {
                val center = Offset(size.width * 0.5f, size.height * 0.72f)
                val dx = maxOf(center.x, size.width - center.x)
                val dy = maxOf(center.y, size.height - center.y)
                val radius = sqrt(dx * dx + dy * dy)
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arc = Size(radius * 2f, radius * 2f)
                // 45 wedges are drawn once into a bitmap; the poster is replayed on every frame of a coverflow move.
                val burst = bakeToBitmap(ceil(size.width).toInt(), ceil(size.height).toInt()) {
                    for (ray in 0 until 45) {
                        drawArc(
                            color = if (ray % 2 == 0) tint.rayA else tint.rayB,
                            startAngle = -90f + ray * 8f,
                            sweepAngle = 8f,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arc,
                        )
                    }
                }
                onDrawBehind { drawImage(burst) }
            },
        contentAlignment = Alignment.Center,
    ) {
        val shadow = with(LocalDensity.current) {
            Shadow(CarteleraPalette.InitialShadow, offset = Offset(0f, 3.dp.toPx()), blurRadius = 0f)
        }
        BasicText(
            initial,
            style = TextStyle(
                color = tint.ink,
                fontSize = 85.sp,
                lineHeight = 85.sp,
                fontFamily = JpFonts.Limelight,
                shadow = shadow,
            ),
        )
    }
}

@Composable
private fun CarteleraArrow(
    forward: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(36.dp)
            .semantics { contentDescription = description }
            .clip(CircleShape)
            .background(CarteleraPalette.ArrowBg)
            .border(1.5.dp, CarteleraPalette.Gold, CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(15.dp)
                .drawWithCache {
                    val unit = size.width / 24f
                    val chevron = Path().apply {
                        if (forward) {
                            moveTo(9f * unit, 5f * unit)
                            lineTo(16f * unit, 12f * unit)
                            lineTo(9f * unit, 19f * unit)
                        } else {
                            moveTo(15f * unit, 5f * unit)
                            lineTo(8f * unit, 12f * unit)
                            lineTo(15f * unit, 19f * unit)
                        }
                    }
                    val line = Stroke(width = 2.6f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    onDrawBehind { drawPath(chevron, CarteleraPalette.BulbOn, style = line) }
                },
        )
    }
}

@Composable
private fun CarteleraFooter(
    network: NetworkStatus,
    nowShowing: String,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val ms = network.routerLatencyMs ?: network.internetLatencyMs
    val ticket = TextStyle(
        color = CarteleraPalette.Gold,
        fontSize = 8.sp,
        fontFamily = JpFonts.Mono,
        letterSpacing = 0.18.em,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 80.dp, end = 80.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            if (ms != null) {
                stringResource(R.string.cartelera_box_office_ms, networkName(network).uppercase(), ms)
            } else {
                stringResource(R.string.cartelera_box_office, networkName(network).uppercase())
            },
            modifier = Modifier
                .dashedPill { false }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
            style = ticket,
        )
        BasicText(
            stringResource(R.string.cartelera_now_showing, nowShowing.uppercase()),
            style = ticket,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                stringResource(R.string.cd_settings).uppercase(),
                modifier = Modifier
                    .themeControlTarget(focus)
                    .dashedPill { focus.onControl }
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = actions::openSettings,
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                style = ticket.copy(color = if (focus.onControl) CarteleraPalette.BulbOn else CarteleraPalette.Gold),
            )
            // Not in the artboard: a second ticket pill so the Themes screen is one D-pad step from Settings.
            BasicText(
                stringResource(R.string.cd_themes).uppercase(),
                modifier = Modifier
                    .themesControlTarget(focus)
                    .dashedPill { focus.onThemes }
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = actions::openThemes,
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                style = ticket.copy(color = if (focus.onThemes) CarteleraPalette.BulbOn else CarteleraPalette.Gold),
            )
        }
    }
}

/** 1 px dashed gold pill outline; [focused] adds the 3 px lamp-colored focus ring 4 px outside it. */
private fun Modifier.dashedPill(focused: () -> Boolean): Modifier = drawWithCache {
    val width = 1.dp.toPx()
    val dashed = Stroke(
        width = width,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
    )
    val ring = Stroke(width = 1.5.dp.toPx())
    val corner = CornerRadius(12.dp.toPx())
    val ringCorner = CornerRadius(14.dp.toPx())
    val outset = 2.dp.toPx()
    onDrawBehind {
        drawRoundRect(
            color = CarteleraPalette.Gold,
            topLeft = Offset(width / 2f, width / 2f),
            size = Size(size.width - width, size.height - width),
            cornerRadius = corner,
            style = dashed,
        )
        if (focused()) {
            drawRoundRect(
                color = CarteleraPalette.BulbOn,
                topLeft = Offset(-outset, -outset),
                size = Size(size.width + outset * 2f, size.height + outset * 2f),
                cornerRadius = ringCorner,
                style = ring,
            )
        }
    }
}
