package co.jp.tv.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Mono-label button of the design: [primary] has the accent border, the other one is a quiet outline. */
@Composable
fun GlassButton(
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    /** Large variant of the design (footer buttons): 8sp label, 17x10dp padding, 16dp radius. */
    large: Boolean = false,
    /**
     * Secondary action on a remote: holding OK or pressing Menu. When set, OK is handled on key release
     * (and not at all after a long press) so holding it never also clicks.
     */
    onLongPress: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val longFired = remember { BooleanArray(1) }
    val p by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "button")
    val shape = RoundedCornerShape(if (large) 16.dp else 15.dp)
    val fill = if (primary) Color(0xFF050506).copy(alpha = 0.7f + 0.2f * p) else JpColors.Paper.copy(alpha = 0.06f + 0.08f * p)
    val borderColor = if (primary) accent.copy(alpha = 0.6f + 0.4f * p) else JpColors.Paper.copy(alpha = 0.2f + 0.6f * p)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = 1f + 0.04f * p; scaleY = 1f + 0.04f * p }
            .background(fill, shape)
            .border(if (focused) 1.5.dp else JpDimens.Hairline, borderColor, shape)
            .onFocusChanged { focused = it.isFocused }
            .then(if (onLongPress == null) Modifier else Modifier.onPreviewKeyEvent { e -> remoteKey(e, longFired, onClick, onLongPress) })
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = if (large) 17.dp else 15.dp, vertical = if (large) 10.dp else 9.dp),
    ) {
        BasicText(
            label,
            style = TextStyle(JpColors.Paper, (if (large) 8f else 7.5f).sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        )
    }
}

private val OkKeys = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)

/** Menu opens the secondary action; OK clicks on release unless it was held long enough to be a long press. */
private fun remoteKey(e: KeyEvent, longFired: BooleanArray, onClick: () -> Unit, onLongPress: () -> Unit): Boolean = when {
    e.key == Key.Menu -> {
        if (e.type == KeyEventType.KeyDown && e.nativeKeyEvent.repeatCount == 0) onLongPress()
        true
    }
    e.key in OkKeys -> {
        when (e.type) {
            KeyEventType.KeyDown -> {
                val repeats = e.nativeKeyEvent.repeatCount
                if (repeats == 0) longFired[0] = false
                if (repeats == 1 && !longFired[0]) {
                    longFired[0] = true
                    onLongPress()
                }
            }
            KeyEventType.KeyUp -> {
                if (!longFired[0]) onClick()
                longFired[0] = false
            }
            else -> Unit
        }
        true
    }
    else -> false
}
