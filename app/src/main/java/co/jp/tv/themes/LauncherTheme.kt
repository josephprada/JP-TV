package co.jp.tv.themes

import androidx.compose.runtime.Composable
import co.jp.tv.themes.shared.DefaultAmbient
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState

/** Every theme draws its own home screen from the same state and actions. */
interface LauncherTheme {
    val id: String
    val displayName: String
    val supportsAccent: Boolean

    /** Which `snd_<set>_*` sound palette this theme plays; each theme has its own voice. */
    val soundSet: String get() = id

    @Composable
    fun Home(state: HomeState, actions: HomeActions)

    /**
     * The theme's background scene with no app UI, for the screensaver's "Ambiente del tema" mode. Draws from
     * [HomeState] only (clock, weather, config); the default is the wallpaper the user picked.
     */
    @Composable
    fun Ambient(state: HomeState) = DefaultAmbient(state)
}
