package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.px

private val Screen = Color(0xFF050805)
private val Phosphor = Color(0xFF33FF66)
private val PhosphorDim = Color(0xFF1F9940)
private const val NBSP = " "

/** Thumbnail of the Terminal home: green phosphor on black, apps listed as commands. */
@Composable
internal fun TerminalPreview(modifier: Modifier = Modifier) {
    val line = TextStyle(color = Phosphor, fontSize = 12.sp, lineHeight = 12.sp, fontFamily = JpFonts.Vt323)
    Column(
        modifier = modifier.background(Screen).fillMaxSize().padding(18.px),
        verticalArrangement = Arrangement.spacedBy(4.px),
    ) {
        BasicText("jp@tv:~$ ls apps/", style = line)
        BasicText(
            "> youtube",
            modifier = Modifier.align(Alignment.Start).background(Phosphor).padding(horizontal = 4.px),
            style = line.copy(color = Screen),
        )
        BasicText("$NBSP${NBSP}netflix", style = line)
        BasicText("$NBSP${NBSP}spotify", style = line)
        BasicText("[ wifi ok · 12ms ]", style = line.copy(color = PhosphorDim))
    }
}
