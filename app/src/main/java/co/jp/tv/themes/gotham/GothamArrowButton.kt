package co.jp.tv.themes.gotham

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Round previous/next arrow (72 design px). Pointer-only: the D-pad already moves the strip,
 * so it never takes focus away from the home root.
 */
@Composable
internal fun GothamArrowButton(
    glyph: GothamGlyph,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(36.dp)
            .semantics { contentDescription = description }
            .background(GothamPalette.ArrowFill.copy(alpha = 0.7f), CircleShape)
            .border(1.dp, GothamPalette.Text.copy(alpha = 0.5f), CircleShape)
            .focusProperties { canFocus = false }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        GothamIcon(glyph, size = 15.dp, color = GothamPalette.Text)
    }
}
