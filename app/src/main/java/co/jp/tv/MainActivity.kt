package co.jp.tv

import android.content.Intent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.jp.tv.apps.ui.EditAppsScreen
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.HomeTransition
import co.jp.tv.core.design.HomeTransitionHost
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.core.util.StartupTrace
import co.jp.tv.core.util.rememberLowEnd
import co.jp.tv.core.util.rememberPausedByLifecycle
import co.jp.tv.home.HomeViewModel
import co.jp.tv.home.Screen
import co.jp.tv.network.ui.NetworkPanel
import co.jp.tv.preferences.ui.CustomizeScreen
import co.jp.tv.settings.domain.SettingsShortcut
import co.jp.tv.settings.domain.SettingsShortcuts
import co.jp.tv.settings.domain.ShortcutTarget
import co.jp.tv.settings.ui.QuickSettingsInfo
import co.jp.tv.settings.ui.QuickSettingsScreen
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.themes.ui.ThemesScreen
import co.jp.tv.wallpaper.WallpaperRegistry
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container get() = (application as JpTvApp).container
    private val viewModel: HomeViewModel by viewModels { container.homeViewModelFactory }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StartupTrace.mark("MainActivity.onCreate")
        container.launchAnchor.attach(window.decorView)
        setContent {
            val screens by viewModel.screens.collectAsStateWithLifecycle()
            BackHandler(enabled = true) { viewModel.back() }

            val state by viewModel.state.collectAsStateWithLifecycle()
            val paused = rememberPausedByLifecycle()
            LaunchedEffect(paused) { viewModel.setActive(!paused) }

            val ready by viewModel.ready.collectAsStateWithLifecycle()
            if (!ready) {
                // Same colour as the window background: nothing is composed until the saved theme is known.
                Box(Modifier.fillMaxSize().background(JpColors.Ink))
                return@setContent
            }
            val accent = Color(state.config.accent)
            val top = screens.lastOrNull()
            val settingsOpen = Screen.Settings in screens
            val networkOpen = top == Screen.Network
            val editOpen = top == Screen.EditApps
            val themesOpen = top == Screen.Themes
            val customizeOpen = top == Screen.Customize
            LaunchedEffect(state.apps.isNotEmpty()) {
                if (state.apps.isNotEmpty()) {
                    StartupTrace.mark("home drawn with ${state.apps.size} apps")
                    reportFullyDrawn()
                }
            }
            val editState by viewModel.editState.collectAsStateWithLifecycle()
            val overlayOpen = screens.isNotEmpty()
            val hideHome = settingsOpen || editOpen || themesOpen || customizeOpen

            val homeFocus = remember { FocusRequester() }

            // Launch exit and return entrance, shared by every theme (see HomeTransition).
            val lowEnd = rememberLowEnd()
            val transition = remember { HomeTransition(lowEnd) }
            val scope = rememberCoroutineScope()
            val view = LocalView.current
            val pausedNow by rememberUpdatedState(paused)
            val homeActions = remember(transition) {
                object : HomeActions by viewModel {
                    override fun launch(app: TvApp) {
                        if (pausedNow) {
                            viewModel.launch(app, null)
                        } else {
                            scope.launch {
                                transition.launchApp(view.width, view.height) { origin -> viewModel.launch(app, origin) }
                            }
                        }
                    }
                }
            }
            LaunchedEffect(paused) { transition.onPausedChanged(paused) }
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
                    if (!customizeOpen) {
                        WallpaperRegistry.byId(state.config.wallpaperId)
                            .Render(accent, (paused && state.config.pauseWallpaperInApps) || overlayOpen, Modifier.fillMaxSize())
                        if (state.config.wallpaperDim > 0) {
                            Box(Modifier.fillMaxSize().background(JpColors.Ink.copy(alpha = state.config.wallpaperDim / 100f)))
                        }
                    }
                    if (!hideHome) {
                        HomeTransitionHost(transition, accent, lowEnd) {
                            Box(Modifier.fillMaxSize().focusRequester(homeFocus).focusRestorer()) {
                                ThemeRegistry.byId(state.config.themeId).Home(state, homeActions)
                            }
                        }
                    }
                    if (settingsOpen && !editOpen && !themesOpen && !customizeOpen) {
                        QuickSettingsScreen(
                            info = QuickSettingsInfo(
                                launcherName = state.brand,
                                launcherVersion = container.deviceInfo.launcherVersion,
                                androidRelease = container.deviceInfo.androidRelease,
                                deviceModel = container.deviceInfo.deviceModel,
                                apps = state.apps.take(5),
                                storage = remember { container.deviceInfo.storage() },
                                display = remember { container.deviceInfo.display() },
                                volume = remember { container.deviceInfo.volume() },
                                bluetoothOn = remember { container.deviceInfo.bluetoothOn() },
                                now = state.now,
                                hourFormat = state.config.hourFormat,
                                titleFontId = state.config.titleFontId,
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
                            config = state.config,
                            onWallpaper = viewModel::setWallpaper,
                            onDim = viewModel::setWallpaperDim,
                            onPauseInApps = viewModel::setPauseWallpaperInApps,
                            onMove = viewModel::moveApp,
                            onSetHidden = viewModel::setAppHidden,
                            onReset = viewModel::resetApps,
                        )
                    }
                    if (themesOpen) {
                        ThemesScreen(
                            activeThemeId = state.config.themeId,
                            accent = accent,
                            onSelect = viewModel::setTheme,
                            onCustomize = viewModel::openCustomize,
                        )
                    }
                    if (customizeOpen) {
                        CustomizeScreen(
                            initial = state.config,
                            onApply = viewModel::applyCustomize,
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

    /** The Home key re-delivers the HOME intent to this singleTask activity: close every panel and show the theme home. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) viewModel.closeAll()
    }

    private fun openShortcut(shortcut: SettingsShortcut) {
        when (val target = shortcut.target) {
            ShortcutTarget.NetworkPanel -> viewModel.openNetwork()
            is ShortcutTarget.Native -> container.settingsLauncher.open(target)
            ShortcutTarget.Themes -> viewModel.openThemes()
            ShortcutTarget.Disabled -> Unit
        }
    }
}
