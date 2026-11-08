package co.jp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.jp.tv.apps.ui.EditAppsScreen
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.core.util.rememberPausedByLifecycle
import co.jp.tv.home.HomeViewModel
import co.jp.tv.home.Screen
import co.jp.tv.network.ui.NetworkPanel
import co.jp.tv.settings.domain.SettingsShortcut
import co.jp.tv.settings.domain.SettingsShortcuts
import co.jp.tv.settings.domain.ShortcutTarget
import co.jp.tv.settings.ui.QuickSettingsInfo
import co.jp.tv.settings.ui.QuickSettingsScreen
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.wallpaper.WallpaperRegistry

class MainActivity : ComponentActivity() {
    private val container get() = (application as JpTvApp).container
    private val viewModel: HomeViewModel by viewModels { container.homeViewModelFactory }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // This is the HOME screen: Back closes an open panel and otherwise does nothing.
            val screens by viewModel.screens.collectAsStateWithLifecycle()
            BackHandler(enabled = true) { viewModel.back() }

            val state by viewModel.state.collectAsStateWithLifecycle()
            val paused = rememberPausedByLifecycle()
            // Probes and animations idle whenever the activity is stopped.
            LaunchedEffect(paused) { viewModel.setActive(!paused) }

            val accent = Color(state.config.accent)
            val settingsOpen = Screen.Settings in screens
            val networkOpen = screens.lastOrNull() == Screen.Network
            val editOpen = screens.lastOrNull() == Screen.EditApps
            val editState by viewModel.editState.collectAsStateWithLifecycle()
            val overlayOpen = screens.isNotEmpty()

            // Focus returns to whatever had it on the home when the last panel closes.
            val homeFocus = remember { FocusRequester() }
            var wasOverlayOpen by remember { mutableStateOf(false) }
            LaunchedEffect(overlayOpen) {
                if (wasOverlayOpen && !overlayOpen) runCatching { homeFocus.requestFocus() }
                wasOverlayOpen = overlayOpen
            }

            CompositionLocalProvider(
                LocalAppIcons provides container.appIconLoader,
                LocalPaused provides paused,
            ) {
                Box(Modifier.fillMaxSize().background(JpColors.Ink)) {
                    WallpaperRegistry.byId(state.config.wallpaperId)
                        .Render(accent, paused, Modifier.fillMaxSize())
                    if (!settingsOpen && !editOpen) {
                        Box(Modifier.fillMaxSize().focusRequester(homeFocus).focusRestorer()) {
                            ThemeRegistry.default.Home(state, viewModel)
                        }
                    }
                    if (settingsOpen && !editOpen) {
                        QuickSettingsScreen(
                            info = QuickSettingsInfo(
                                launcherName = state.brand,
                                launcherVersion = container.deviceInfo.launcherVersion,
                                androidRelease = container.deviceInfo.androidRelease,
                                deviceModel = container.deviceInfo.deviceModel,
                                appCount = editState.visible.size + editState.hidden.size,
                                storage = remember { container.deviceInfo.storage() },
                                display = remember { container.deviceInfo.display() },
                            ),
                            network = state.network,
                            accent = accent,
                            onShortcut = { openShortcut(it) },
                            onEditApps = { viewModel.openEditApps(null) },
                            onAllSettings = {
                                container.settingsLauncher.open(
                                    SettingsShortcuts.ALL_SETTINGS_ACTION,
                                    SettingsShortcuts.ALL_SETTINGS_ACTION,
                                )
                            },
                        )
                    }
                    if (editOpen) {
                        EditAppsScreen(
                            apps = editState,
                            freeGb = remember { container.deviceInfo.storage()?.let { SettingsShortcuts.formatGb(it.freeBytes) } },
                            accent = accent,
                            initialFocus = viewModel.editFocusPackage,
                            onMove = viewModel::moveApp,
                            onSetHidden = viewModel::setAppHidden,
                            onReset = viewModel::resetApps,
                        )
                    }
                    if (networkOpen) {
                        NetworkPanel(
                            status = state.network,
                            accent = accent,
                            onTest = viewModel::testConnection,
                            onWifiSettings = {
                                container.settingsLauncher.open(
                                    SettingsShortcuts.WIFI_SETTINGS_ACTION,
                                    SettingsShortcuts.ALL_SETTINGS_ACTION,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    private fun openShortcut(shortcut: SettingsShortcut) {
        when (val target = shortcut.target) {
            ShortcutTarget.NetworkPanel -> viewModel.openNetwork()
            is ShortcutTarget.Native -> container.settingsLauncher.open(target)
            ShortcutTarget.Disabled -> Unit
        }
    }
}
