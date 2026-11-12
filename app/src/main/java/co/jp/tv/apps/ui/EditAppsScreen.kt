package co.jp.tv.apps.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.AppLayout
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.preferences.ui.rememberMediaPermissionRequester
import co.jp.tv.wallpaper.WallpaperRegistry

private val PillShape = RoundedCornerShape(11.dp)
private val RowShape = RoundedCornerShape(12.dp)
private val InkGlass = Color(0xFF050506).copy(alpha = 0.7f)

/**
 * App editor. D-pad model: Up/Down walk the list, Right on a rail row moves focus to the hidden
 * column and Left comes back. OK on a rail row starts "move mode" (Up/Down reorder, Right hides,
 * OK or Back confirms); OK on a hidden row shows it again. Every change is persisted by the caller
 * right away. dp = design px / 2.
 */
@Composable
fun EditAppsScreen(
    apps: AppLayout.ResolvedApps,
    freeGb: String?,
    accent: Color,
    initialFocus: String?,
    config: LauncherConfig,
    onWallpaper: (String) -> Unit,
    onDim: (Int) -> Unit,
    onPauseInApps: (Boolean) -> Unit,
    onMove: (packageName: String, up: Boolean) -> Unit,
    onSetHidden: (packageName: String, hidden: Boolean) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rootFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { rootFocus.requestFocus() }

    var focusedPackage by remember { mutableStateOf(initialFocus) }
    var movingFlag by remember { mutableStateOf(false) }
    var resetSelected by remember { mutableStateOf(false) }
    var resetArmed by remember { mutableStateOf(false) }
    // Slot of the wallpaper column ([WallSlot]) while D-pad focus is there; null in the app columns.
    var wallSlot by remember { mutableStateOf<Int?>(null) }
    val requestMedia = rememberMediaPermissionRequester { }

    val railPackages = apps.visible.map { it.packageName }
    val hiddenPackages = apps.hidden.map { it.packageName }
    val focus = focusedPackage?.takeIf { it in railPackages || it in hiddenPackages }
        ?: railPackages.firstOrNull() ?: hiddenPackages.firstOrNull()
    val inRail = focus != null && focus in railPackages
    val moving = movingFlag && inRail && !resetSelected

    BackHandler(enabled = moving) { movingFlag = false }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JpColors.Ink.copy(alpha = 0.5f))
            .padding(20.dp)
            .focusRequester(rootFocus)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val key = event.key
                val isOk = key == Key.DirectionCenter || key == Key.Enter || key == Key.NumPadEnter
                if (isOk && event.nativeKeyEvent.repeatCount > 0) return@onKeyEvent true
                val handled = key in HANDLED_KEYS || isOk
                if (!handled) return@onKeyEvent false
                wallSlot?.let { slot ->
                    val walls = WallpaperRegistry.all
                    when {
                        key == Key.DirectionLeft -> when {
                            slot == WallSlot.Dim -> onDim(LauncherConfig.stepDim(config.wallpaperDim, -1))
                            slot in 1 until WallSlot.chips -> wallSlot = slot - 1
                            slot == 0 -> wallSlot = null
                        }
                        key == Key.DirectionRight -> when {
                            slot == WallSlot.Dim -> onDim(LauncherConfig.stepDim(config.wallpaperDim, 1))
                            slot < WallSlot.chips - 1 -> wallSlot = slot + 1
                        }
                        key == Key.DirectionDown -> if (slot < WallSlot.Pause) wallSlot = if (slot < WallSlot.chips) WallSlot.Dim else WallSlot.Pause
                        key == Key.DirectionUp -> if (slot == WallSlot.Pause) wallSlot = WallSlot.Dim
                            else if (slot == WallSlot.Dim) wallSlot = walls.indexOfFirst { it.id == config.wallpaperId }.coerceAtLeast(0)
                        isOk -> when {
                            slot < WallSlot.chips -> walls[slot].let { onWallpaper(it.id); requestMedia(it.id) }
                            slot == WallSlot.Pause -> onPauseInApps(!config.pauseWallpaperInApps)
                        }
                    }
                    return@onKeyEvent true
                }
                if (!(isOk && resetSelected)) resetArmed = false
                val list = if (inRail) railPackages else hiddenPackages
                val index = list.indexOf(focus)
                when {
                    key == Key.DirectionUp -> when {
                        resetSelected -> Unit
                        moving -> focus?.let { onMove(it, true) }
                        index > 0 -> focusedPackage = list[index - 1]
                        else -> resetSelected = true
                    }
                    key == Key.DirectionDown -> when {
                        resetSelected -> { resetSelected = false; focusedPackage = railPackages.firstOrNull() ?: hiddenPackages.firstOrNull() }
                        moving -> focus?.let { onMove(it, false) }
                        index in 0 until list.lastIndex -> focusedPackage = list[index + 1]
                    }
                    key == Key.DirectionRight -> when {
                        resetSelected -> Unit
                        moving -> focus?.let { onSetHidden(it, true) }.also { movingFlag = false }
                        inRail && hiddenPackages.isNotEmpty() ->
                            focusedPackage = hiddenPackages[index.coerceAtMost(hiddenPackages.lastIndex)]
                        else -> wallSlot = WallpaperRegistry.all.indexOfFirst { it.id == config.wallpaperId }.coerceAtLeast(0)
                    }
                    key == Key.DirectionLeft -> when {
                        resetSelected || moving -> Unit
                        !inRail && railPackages.isNotEmpty() ->
                            focusedPackage = railPackages[index.coerceAtMost(railPackages.lastIndex)]
                    }
                    isOk -> when {
                        resetSelected -> if (resetArmed) { onReset(); resetArmed = false; resetSelected = false } else resetArmed = true
                        moving -> movingFlag = false
                        inRail -> movingFlag = true
                        focus != null -> onSetHidden(focus, false)
                    }
                }
                true
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(
            visible = railPackages.size,
            hidden = hiddenPackages.size,
            freeGb = freeGb,
            moving = moving,
            accent = accent,
            resetFocused = resetSelected,
            resetArmed = resetArmed,
        )
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppColumn(
                title = stringResource(R.string.edit_col_rail, railPackages.size),
                emptyText = stringResource(R.string.edit_empty_rail),
                apps = apps.visible,
                focus = focus.takeIf { !resetSelected && wallSlot == null },
                moving = moving,
                numbered = true,
                accent = accent,
                riseDelayMs = 100,
                modifier = Modifier.weight(1f),
            )
            AppColumn(
                title = stringResource(R.string.edit_col_hidden, hiddenPackages.size),
                emptyText = stringResource(R.string.edit_empty_hidden),
                apps = apps.hidden,
                focus = focus.takeIf { !resetSelected && wallSlot == null },
                moving = false,
                numbered = false,
                accent = accent,
                riseDelayMs = 180,
                modifier = Modifier.weight(1f),
            )
            EditWallpaperColumn(config, accent, wallSlot, Modifier.weight(1f))
        }
    }
}

private val HANDLED_KEYS = setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)

@Composable
private fun Header(
    visible: Int,
    hidden: Int,
    freeGb: String?,
    moving: Boolean,
    accent: Color,
    resetFocused: Boolean,
    resetArmed: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp)
            .riseIn(delayMs = 0, durationMs = JpMotion.RiseMs, distanceDp = 8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText(
                stringResource(R.string.edit_title),
                style = TextStyle(JpColors.Paper, 20.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
            )
            BasicText(
                stringResource(if (moving) R.string.edit_hint_moving else R.string.edit_breadcrumb),
                style = TextStyle(if (moving) accent else JpColors.Faint, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Pill(pluralStringResource(R.plurals.edit_pill_visible, visible, visible), strong = true)
            Pill(pluralStringResource(R.plurals.edit_pill_hidden, hidden, hidden))
            if (freeGb != null) Pill(stringResource(R.string.edit_free_gb, freeGb))
            ResetButton(focused = resetFocused, armed = resetArmed, accent = accent)
        }
    }
}

@Composable
private fun Pill(text: String, strong: Boolean = false) {
    BasicText(
        text,
        style = TextStyle(if (strong) JpColors.Paper else JpColors.Muted, 9.sp, fontFamily = JpFonts.Body),
        modifier = Modifier
            .background(Color(0xFF121214).copy(alpha = 0.45f), PillShape)
            .border(JpDimens.Hairline, JpColors.Line, PillShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

/** Not a real focus target: the screen's key handler tells it when it is selected. */
@Composable
private fun ResetButton(focused: Boolean, armed: Boolean, accent: Color) {
    val p by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "reset")
    val shape = RoundedCornerShape(11.dp)
    BasicText(
        stringResource(if (armed) R.string.edit_reset_confirm else R.string.edit_reset),
        style = TextStyle(if (armed) accent else JpColors.Paper, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        modifier = Modifier
            .graphicsLayer { scaleX = 1f + 0.04f * p; scaleY = 1f + 0.04f * p }
            .background(JpColors.Paper.copy(alpha = 0.06f + 0.08f * p), shape)
            .border(if (focused) 1.5.dp else JpDimens.Hairline, JpColors.Paper.copy(alpha = 0.2f + 0.6f * p), shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun AppColumn(
    title: String,
    emptyText: String,
    apps: List<TvApp>,
    focus: String?,
    moving: Boolean,
    numbered: Boolean,
    accent: Color,
    riseDelayMs: Int,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val focusIndex = apps.indexOfFirst { it.packageName == focus }
    KeepInView(listState, focusIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .riseIn(delayMs = riseDelayMs, durationMs = JpMotion.RiseMs, distanceDp = 9f)
            .glass(RoundedCornerShape(JpDimens.RailRadius))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        BasicText(
            title,
            style = TextStyle(JpColors.Faint, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
            modifier = Modifier.padding(start = 6.dp, bottom = 7.dp),
        )
        if (apps.isEmpty()) {
            BasicText(
                emptyText,
                style = TextStyle(JpColors.Faint, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                modifier = Modifier.padding(start = 6.dp, top = 4.dp),
            )
        }
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                val isFocused = app.packageName == focus
                EditRow(
                    app = app,
                    number = if (numbered) "%02d".format(index + 1) else null,
                    focused = isFocused,
                    moving = isFocused && moving,
                    accent = accent,
                    tag = when {
                        !isFocused -> null
                        moving -> stringResource(R.string.edit_tag_moving)
                        numbered -> stringResource(R.string.edit_tag_move)
                        else -> stringResource(R.string.edit_tag_show)
                    },
                    compact = !numbered,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

/** Scrolls the list just enough to keep the focused row visible. */
@Composable
private fun KeepInView(state: LazyListState, index: Int) {
    LaunchedEffect(index) {
        if (index < 0) return@LaunchedEffect
        val visible = state.layoutInfo.visibleItemsInfo
        val first = visible.firstOrNull()?.index ?: return@LaunchedEffect
        val last = visible.last().index
        if (index <= first || index >= last) state.animateScrollToItem((index - 3).coerceAtLeast(0))
    }
}

@Composable
private fun EditRow(
    app: TvApp,
    number: String?,
    focused: Boolean,
    moving: Boolean,
    accent: Color,
    tag: String?,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val focus by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "editFocus")
    val move by animateFloatAsState(if (moving) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "editMove")

    // Gentle nudge while moving; idles when the activity is stopped.
    val paused = LocalPaused.current
    val nudge = remember { Animatable(0f) }
    LaunchedEffect(moving, paused) {
        if (!moving || paused) {
            nudge.snapTo(0f)
            return@LaunchedEffect
        }
        while (true) {
            nudge.animateTo(1f, tween(1200, easing = JpMotion.InOut))
            nudge.animateTo(0f, tween(1200, easing = JpMotion.InOut))
        }
    }
    val liftPx = with(LocalDensity.current) { 3.dp.toPx() }

    val height = if (compact) 31.dp else 39.dp
    val fill = lerp(lerp(Color.Transparent, JpColors.FocusFill, focus), InkGlass, move)
    val ring = lerp(JpColors.Paper.copy(alpha = 0.3f * focus), accent.copy(alpha = 0.7f), move)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                val s = 1f + 0.03f * move
                scaleX = s
                scaleY = s
                translationY = -liftPx * nudge.value
            }
            .background(fill, RowShape)
            .border(JpDimens.Hairline, ring, RowShape)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (number != null) {
            BasicText(number, style = TextStyle(JpColors.Faint, 6.5.sp, fontFamily = JpFonts.Mono), modifier = Modifier.size(width = 13.dp, height = 12.dp))
        }
        EditIcon(app, move, compact, accent)
        BasicText(
            app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = lerp(if (compact) JpColors.Faint else JpColors.Muted, JpColors.Paper, maxOf(focus, move)),
                fontSize = if (compact) 11.sp else 12.sp,
                fontFamily = JpFonts.Body,
            ),
            modifier = Modifier.weight(1f),
        )
        if (tag != null) {
            BasicText(
                tag,
                style = TextStyle(if (moving) JpColors.AccentLift else JpColors.Faint, 6.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
            )
        }
    }
}

@Composable
private fun EditIcon(app: TvApp, move: Float, compact: Boolean, accent: Color) {
    val icons = LocalAppIcons.current
    val icon by produceState<ImageBitmap?>(null, app.packageName) { value = icons.load(app) }
    val side = if (compact) 18.dp else 23.dp
    val shape = RoundedCornerShape(if (compact) 6.dp else 7.dp)
    Box(
        modifier = Modifier
            .size(side)
            .clip(shape)
            .background(lerp(if (compact) Color.Transparent else JpColors.TileFill, accent, move))
            .border(JpDimens.Hairline, JpColors.Line.copy(alpha = JpColors.Line.alpha * (1f - move)), shape),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = icon
        if (bitmap != null) {
            Image(bitmap, contentDescription = null, modifier = Modifier.size(side - 4.dp))
        } else {
            BasicText(
                app.label.filter { it.isLetter() }.take(2).uppercase(),
                style = TextStyle(lerp(JpColors.Paper, JpColors.Ink, move), 7.sp, fontFamily = JpFonts.Mono),
            )
        }
    }
}
