package co.jp.tv.preferences.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.preferences.domain.LauncherConfig

/**
 * Launcher name field, made for a remote: the box takes D-pad focus like any control, OK starts editing
 * (and raises the on-screen keyboard), keyboard Done or Back stops editing and gives focus back to the box
 * so the D-pad keeps working.
 */
@Composable
internal fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    titleFont: FontFamily,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    var boxFocused by remember { mutableStateOf(false) }
    // The input held focus at some point of this edit: ending the edit then hands focus back to the box.
    var inputHadFocus by remember { mutableStateOf(false) }
    val boxFocus = remember { FocusRequester() }
    val inputFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val label = stringResource(R.string.customize_brand)
    val shape = RoundedCornerShape(11.dp)

    fun stopEditing() {
        editing = false
        keyboard?.hide()
    }

    LaunchedEffect(editing) {
        if (editing) {
            inputFocus.requestFocus()
            keyboard?.show()
        } else if (inputHadFocus) {
            inputHadFocus = false
            boxFocus.requestFocus()
        }
    }
    BackHandler(enabled = editing) { stopEditing() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF050506).copy(alpha = 0.6f), shape)
            .border(if (boxFocused || editing) 1.5.dp else 1.dp, accent.copy(alpha = if (boxFocused || editing) 1f else 0.6f), shape)
            .focusRequester(boxFocus)
            .onFocusChanged { boxFocused = it.isFocused }
            .semantics { contentDescription = label }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { editing = true }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(LauncherConfig.MAX_BRAND)) },
            enabled = editing,
            singleLine = true,
            textStyle = TextStyle(color = accent, fontSize = 19.sp, fontFamily = titleFont, letterSpacing = (-0.01).em),
            cursorBrush = SolidColor(accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { stopEditing() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(inputFocus)
                .onFocusChanged { if (it.isFocused) inputHadFocus = true },
        )
    }
}
