package co.jp.tv

import android.Manifest
import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.jp.tv.apps.ui.EditAppsScreen
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.HomeTransition
import co.jp.tv.core.design.HomeTransitionHost
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.sound.LocalUiSounds
import co.jp.tv.core.sound.UiSound
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.core.util.StartupTrace
import co.jp.tv.core.util.rememberLowEnd
import co.jp.tv.core.util.rememberPausedByLifecycle
import co.jp.tv.home.HomeViewModel
import co.jp.tv.home.Screen
import co.jp.tv.homeoverride.domain.HomeOverrideState
import co.jp.tv.homeoverride.ui.HomeOverrideUi
import co.jp.tv.network.ui.NetworkPanel
import co.jp.tv.playlist.ui.LocalMediaThumbnails
import co.jp.tv.playlist.ui.LocalPlaylistStateStore
import co.jp.tv.playlist.ui.LocalWallpaperMedia
import co.jp.tv.playlist.domain.WallpaperMedia
import co.jp.tv.preferences.ui.CustomizeScreen
import co.jp.tv.preferences.ui.rememberMediaPermissionRequester
import co.jp.tv.screensaver.domain.IdleRules
import co.jp.tv.screensaver.domain.SaverKeyGate
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverState
import co.jp.tv.screensaver.ui.ScreensaverHost
import co.jp.tv.screensaver.ui.ScreensaverSettingsScreen
import co.jp.tv.settings.domain.SettingsShortcut
import co.jp.tv.settings.domain.SettingsShortcuts
import co.jp.tv.settings.domain.ShortcutTarget
import co.jp.tv.settings.ui.QuickSettingsInfo
import co.jp.tv.settings.ui.QuickSettingsScreen
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.themes.ui.ThemesScreen
import co.jp.tv.wallpaper.WallpaperRegistry
import co.jp.tv.wallpaper.image.StaticImageWallpaper
import co.jp.tv.wallpaper.video.VideoWallpaper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container get() = (application as JpTvApp).container
    private val viewModel: HomeViewModel by viewModels { container.homeViewModelFactory }
    private var locationAsked = false

    // In-app screensaver: shown after the idle time on the home, ended (and the key swallowed) by any key.
    private val saverShowing = mutableStateOf(false)
    private val keyGate = SaverKeyGate()

    @Volatile private var lastInputMs = SystemClock.elapsedRealtime()

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

            val library by container.mediaCatalog.library.collectAsStateWithLifecycle()
            val ready by viewModel.ready.collectAsStateWithLifecycle()
            if (!ready) {
                // Same colour as the window background: nothing is composed until the saved theme is known.
                Box(Modifier.fillMaxSize().background(JpColors.Ink))
                return@setContent
            }
            val accent = Color(state.config.accent)
            val sounds = container.uiSounds
            val theme = ThemeRegistry.byId(state.config.themeId)
            // Only the active theme's clips stay loaded; nothing sounds while the launcher is stopped.
            LaunchedEffect(theme.soundSet) { sounds.useSet(theme.soundSet) }
            LaunchedEffect(state.config.soundsEnabled, state.config.soundVolume) {
                sounds.configure(state.config.soundsEnabled, state.config.soundVolume)
            }
            LaunchedEffect(paused) { sounds.setActive(!paused) }
            val top = screens.lastOrNull()
            val settingsOpen = Screen.Settings in screens
            val networkOpen = top == Screen.Network
            val editOpen = top == Screen.EditApps
            val themesOpen = top == Screen.Themes
            val customizeOpen = top == Screen.Customize
            val screensaverOpen = top == Screen.Screensaver
            val showingSaver = saverShowing.value
            LaunchedEffect(state.apps.isNotEmpty()) {
                if (state.apps.isNotEmpty()) {
                    StartupTrace.mark("home drawn with ${state.apps.size} apps")
                    reportFullyDrawn()
                }
            }
            // Heartbeat for the home override's safety guards: the home is on screen (also when resuming).
            LaunchedEffect(paused, state.apps.isNotEmpty()) {
                if (!paused && state.apps.isNotEmpty()) container.redirectPolicy.onHomeDrawn()
            }
            // Usage-stats backend of the home override: runs while the toggle and both special accesses are on.
            val homeOverrideEnabled = state.config.homeOverrideEnabled
            LaunchedEffect(paused, homeOverrideEnabled) { if (!paused) container.foregroundWatcher.sync(homeOverrideEnabled) }
            // Look for wallpaper media whenever the home comes to the front (a USB drive may have been plugged in).
            LaunchedEffect(paused) { if (!paused) container.mediaCatalog.refresh() }
            val wallpaperMedia = remember(library, state.config.videoPlaylist, state.config.imagePlaylist) {
                WallpaperMedia.of(library, state.config.videoPlaylist, state.config.imagePlaylist)
            }
            val locationRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                viewModel.refreshNetworkAccess()
            }
            val askForLocation = {
                locationRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            // The Wi-Fi name needs location access: ask once, when the user opens the network panel (their own
            // action, so the dialog is expected). The panel's button asks again on demand.
            LaunchedEffect(networkOpen) {
                if (networkOpen && !locationAsked && !container.locationAccess.permissionGranted()) {
                    locationAsked = true
                    askForLocation()
                }
            }
            // The saver ends when the launcher goes to the background (an app, or the system's own screensaver, took over).
            LaunchedEffect(paused) { if (paused) saverShowing.value = false }
            // Idle on the home with nothing open: after the chosen minutes without a key, start the saver.
            val homeIdle = !paused && screens.isEmpty() && state.apps.isNotEmpty()
            val idleMinutes = state.config.screensaver.idleMinutes
            LaunchedEffect(homeIdle, idleMinutes, showingSaver) {
                if (!homeIdle || showingSaver) return@LaunchedEffect
                // Arriving at the home counts as activity.
                lastInputMs = SystemClock.elapsedRealtime()
                while (true) {
                    val now = SystemClock.elapsedRealtime()
                    val wait = IdleRules.msUntilStart(lastInputMs, now, idleMinutes) ?: return@LaunchedEffect
                    if (IdleRules.shouldStart(lastInputMs, now, idleMinutes, homeVisible = true, saverShowing = false)) {
                        saverShowing.value = true
                        return@LaunchedEffect
                    }
                    delay(wait)
                }
            }
            val editState by viewModel.editState.collectAsStateWithLifecycle()
            val overlayOpen = screens.isNotEmpty()
            val hideHome = settingsOpen || editOpen || themesOpen || customizeOpen || screensaverOpen

            val homeFocus = remember { FocusRequester() }

            // Launch exit and return entrance, shared by every theme (see HomeTransition).
            val lowEnd = rememberLowEnd()
            val transition = remember { HomeTransition(lowEnd) }
            val scope = rememberCoroutineScope()
            val requestMedia = rememberMediaPermissionRequester { scope.launch { container.mediaCatalog.refresh() } }
            val view = LocalView.current
            val pausedNow by rememberUpdatedState(paused)
            val homeActions = remember(transition, sounds) {
                object : HomeActions by viewModel {
                    override fun launch(app: TvApp) {
                        sounds.play(UiSound.Select)
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
                // While the saver covers the home, the home's own animations stand still.
                LocalPaused provides (paused || showingSaver),
                LocalUiSounds provides sounds,
                LocalWallpaperMedia provides wallpaperMedia,
                LocalPlaylistStateStore provides container.playlistStateStore,
                LocalMediaThumbnails provides container.mediaThumbnails,
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(JpColors.Ink)
                        // Screens drawn over the home (settings, themes, customize...) share the home's voice:
                        // a tick for every D-pad press and a click for OK. The home itself sounds from the view
                        // model (selection changes) so it stays silent at the ends of a list.
                        .onPreviewKeyEvent { event ->
                            if (overlayOpen && event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight ->
                                        sounds.play(UiSound.Move)
                                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter ->
                                        if (event.nativeKeyEvent.repeatCount == 0) sounds.play(UiSound.Select)
                                    else -> Unit
                                }
                            }
                            false
                        },
                ) {
                    // Under the saver the wallpaper is not composed at all: its video player must not hold a second decoder.
                    if (!customizeOpen && !screensaverOpen && !showingSaver) {
                        WallpaperRegistry.byId(state.config.wallpaperId)
                            .Render(accent, (paused && state.config.pauseWallpaperInApps) || overlayOpen, Modifier.fillMaxSize())
                        if (state.config.wallpaperDim > 0) {
                            Box(Modifier.fillMaxSize().background(JpColors.Ink.copy(alpha = state.config.wallpaperDim / 100f)))
                        }
                    }
                    if (!hideHome) {
                        HomeTransitionHost(transition, accent, lowEnd) {
                            Box(Modifier.fillMaxSize().focusRequester(homeFocus).focusRestorer()) {
                                theme.Home(state, homeActions)
                            }
                        }
                    }
                    if (settingsOpen && !editOpen && !themesOpen && !customizeOpen && !screensaverOpen) {
                        // Read-only checks, refreshed whenever the user comes back from an Android settings screen.
                        var accessibilityOn by remember { mutableStateOf(false) }
                        var usageAccess by remember { mutableStateOf(false) }
                        var overlay by remember { mutableStateOf(false) }
                        LaunchedEffect(paused) {
                            if (!paused) {
                                accessibilityOn = container.accessibilityStatus.isServiceEnabled()
                                usageAccess = container.watcherPermissions.usageAccess()
                                overlay = container.watcherPermissions.overlay()
                            }
                        }
                        val homeOverrideOn = state.config.homeOverrideEnabled
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
                            homeOverride = HomeOverrideUi(
                                state = HomeOverrideState.of(homeOverrideOn, accessibilityOn, usageAccess, overlay),
                                onToggle = { viewModel.setHomeOverride(!homeOverrideOn) },
                                onGrantUsageAccess = {
                                    container.settingsLauncher.openForThisApp(
                                        android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS,
                                        android.provider.Settings.ACTION_SETTINGS,
                                    )
                                },
                                onGrantOverlay = {
                                    container.settingsLauncher.openForThisApp(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.provider.Settings.ACTION_SETTINGS,
                                    )
                                },
                                onOpenStockLauncher = { container.stockLauncherOpener.open() },
                            ),
                            screensaver = state.config.screensaver,
                            onOpenScreensaver = viewModel::openScreensaver,
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
                            media = library,
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
                            searchPlaces = viewModel::searchPlaces,
                            media = library,
                            onRescan = { scope.launch { container.mediaCatalog.refresh() } },
                        )
                    }
                    if (screensaverOpen) {
                        LaunchedEffect(Unit) { container.mediaCatalog.refresh() }
                        ScreensaverSettingsScreen(
                            settings = state.config.screensaver,
                            accent = accent,
                            titleFontId = state.config.titleFontId,
                            onChange = { next -> viewModel.setScreensaver { next } },
                            onPreview = { saverShowing.value = true },
                            onOpenSystemSettings = { container.settingsLauncher.openScreensaverSettings() },
                            onModeChosen = { mode ->
                                when (mode) {
                                    ScreensaverMode.PHOTOS -> requestMedia(StaticImageWallpaper.ID)
                                    ScreensaverMode.LIVE -> requestMedia(VideoWallpaper.ID)
                                    else -> Unit
                                }
                            },
                        )
                    }
                    if (networkOpen) {
                        NetworkPanel(
                            status = state.network,
                            accent = accent,
                            onTest = viewModel::testConnection,
                            onLocationSettings = {
                                container.settingsLauncher.open(
                                    SettingsShortcuts.LOCATION_SETTINGS_ACTION,
                                    SettingsShortcuts.ALL_SETTINGS_ACTION,
                                )
                            },
                            onGrantLocation = askForLocation,
                            onWifiSettings = {
                                container.settingsLauncher.open(
                                    SettingsShortcuts.WIFI_SETTINGS_ACTION,
                                    SettingsShortcuts.ALL_SETTINGS_ACTION,
                                )
                            },
                        )
                    }
                    if (showingSaver) {
                        // Collected only while it shows; starts from what the home already knows so the mode never flashes.
                        val first = remember { ScreensaverState.of(state.now, state.config, state.weather, library) }
                        val saverState by remember { container.screensaverStates() }.collectAsStateWithLifecycle(initialValue = first)
                        // The saver's own animations follow the lifecycle, not the home's "covered" pause.
                        CompositionLocalProvider(LocalPaused provides paused) {
                            ScreensaverHost(saverState, Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        when (keyGate.verdict(saverShowing.value, event.action == KeyEvent.ACTION_DOWN, event.keyCode, event.repeatCount)) {
            SaverKeyGate.Verdict.PASS -> {
                lastInputMs = SystemClock.elapsedRealtime()
                super.dispatchKeyEvent(event)
            }
            SaverKeyGate.Verdict.CONSUME -> true
            SaverKeyGate.Verdict.DISMISS -> {
                lastInputMs = SystemClock.elapsedRealtime()
                saverShowing.value = false
                true
            }
        }

    override fun onUserInteraction() {
        super.onUserInteraction()
        lastInputMs = SystemClock.elapsedRealtime()
    }

    /** The Home key re-delivers the HOME intent to this singleTask activity: close every panel and show the theme home. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            saverShowing.value = false
            viewModel.closeAll()
            // Already in front, so no lifecycle change will report it: tell the home override guards.
            container.redirectPolicy.onHomeDrawn()
        }
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
