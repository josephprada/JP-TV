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
) {
    var focused by remember { mutableStateOf(false) }
    val p by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "button")
    val shape = RoundedCornerShape(15.dp)
    val fill = if (primary) Color(0xFF050506).copy(alpha = 0.7f + 0.2f * p) else JpColors.Paper.copy(alpha = 0.06f + 0.08f * p)
    val borderColor = if (primary) accent.copy(alpha = 0.6f + 0.4f * p) else JpColors.Paper.copy(alpha = 0.2f + 0.6f * p)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = 1f + 0.04f * p; scaleY = 1f + 0.04f * p }
            .background(fill, shape)
            .border(if (focused) 1.5.dp else JpDimens.Hairline, borderColor, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 9.dp),
    ) {
        BasicText(
            label,
            style = TextStyle(JpColors.Paper, 7.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        )
    }
}
