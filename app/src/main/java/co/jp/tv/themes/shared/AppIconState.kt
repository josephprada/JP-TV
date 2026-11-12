package co.jp.tv.themes.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.LocalAppIcons

/** The app's real launcher icon, loaded once off the main thread; null until it arrives or when there is none. */
@Composable
fun rememberAppIcon(app: TvApp): State<ImageBitmap?> {
    val icons = LocalAppIcons.current
    return produceState<ImageBitmap?>(null, app.packageName) { value = icons.load(app) }
}
