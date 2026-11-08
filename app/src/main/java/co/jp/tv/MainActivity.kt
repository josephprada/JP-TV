package co.jp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.home.HomeViewModel
import co.jp.tv.themes.ThemeRegistry

class MainActivity : ComponentActivity() {
    private val container get() = (application as JpTvApp).container
    private val viewModel: HomeViewModel by viewModels { container.homeViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // This is the HOME screen: Back must do nothing.
            BackHandler {}
            val state by viewModel.state.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalAppIcons provides container.appIconLoader) {
                ThemeRegistry.default.Home(state, viewModel)
            }
        }
    }
}
