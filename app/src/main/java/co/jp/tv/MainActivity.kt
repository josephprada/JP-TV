package co.jp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B0B0C)
private val Accent = Color(0xFFE63329)
private val Subtitle = Color(0xFFB5B5B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PlaceholderHome() }
    }
}

@Composable
private fun PlaceholderHome() {
    Column(
        modifier = Modifier.fillMaxSize().background(Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = stringResource(R.string.app_name),
            style = TextStyle(color = Accent, fontSize = 96.sp, fontWeight = FontWeight.Black),
        )
        BasicText(
            text = stringResource(R.string.phase_0_subtitle),
            style = TextStyle(color = Subtitle, fontSize = 24.sp),
        )
    }
}
