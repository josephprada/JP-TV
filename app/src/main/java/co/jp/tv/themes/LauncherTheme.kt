package co.jp.tv.themes

import androidx.compose.runtime.Composable
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState

/** Every theme draws its own home screen from the same state and actions. */
interface LauncherTheme {
    val id: String
    val displayName: String
    val supportsAccent: Boolean

    @Composable
    fun Home(state: HomeState, actions: HomeActions)
}
