package co.jp.tv

import android.content.Context
import android.os.SystemClock
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.jp.tv.apps.data.AppIconLoader
import co.jp.tv.apps.data.FileAppListStore
import co.jp.tv.apps.data.LaunchAnchor
import co.jp.tv.apps.data.PackageManagerAppCatalog
import co.jp.tv.apps.domain.CachedAppCatalog
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.core.sound.UiSounds
import co.jp.tv.core.sound.data.SoundPoolUiSounds
import co.jp.tv.core.util.ClockTicker
import co.jp.tv.home.HomeViewModel
import co.jp.tv.homeoverride.data.AccessibilityStatusReader
import co.jp.tv.homeoverride.data.ForegroundWatcherController
import co.jp.tv.homeoverride.data.HomeStarter
import co.jp.tv.homeoverride.data.SharedPrefsGuardStateStore
import co.jp.tv.homeoverride.data.StockHomeResolver
import co.jp.tv.homeoverride.data.StockLauncherOpener
import co.jp.tv.homeoverride.data.WatcherPermissions
import co.jp.tv.homeoverride.domain.RedirectPolicy
import co.jp.tv.network.data.ConnectivityNetworkMonitor
import co.jp.tv.network.data.LocationAccess
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.playlist.data.FileMediaCatalog
import co.jp.tv.playlist.data.MediaThumbnailLoader
import co.jp.tv.playlist.data.SharedPrefsPlaylistStateStore
import co.jp.tv.playlist.domain.MediaCatalog
import co.jp.tv.playlist.domain.PlaylistStateStore
import co.jp.tv.playlist.ui.MediaThumbnails
import co.jp.tv.preferences.data.DataStoreConfigStore
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.screensaver.domain.ScreensaverState
import co.jp.tv.screensaver.domain.ScreensaverStates
import co.jp.tv.settings.data.DeviceInfo
import co.jp.tv.settings.data.SettingsLauncher
import co.jp.tv.weather.data.FileWeatherCache
import co.jp.tv.weather.data.HttpUrlConnectionTransport
import co.jp.tv.weather.data.OpenMeteoPlaceSearch
import co.jp.tv.weather.data.OpenMeteoWeatherRemote
import co.jp.tv.weather.domain.CachedWeatherRepository
import co.jp.tv.weather.domain.PlaceSearch
import co.jp.tv.weather.domain.WeatherRepository
import kotlinx.coroutines.flow.Flow

private val Context.launcherDataStore by preferencesDataStore(name = "launcher")

/**
 * Manual dependency holder. The only place that creates adapters and wires them to ports.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val launchAnchor = LaunchAnchor()
    val appCatalog: AppCatalog = CachedAppCatalog(PackageManagerAppCatalog(appContext, launchAnchor), FileAppListStore(appContext))
    val configStore: ConfigStore = DataStoreConfigStore(appContext.launcherDataStore)
    val appIconLoader = AppIconLoader(appContext)
    val locationAccess = LocationAccess(appContext)
    val networkMonitor: NetworkMonitor = ConnectivityNetworkMonitor(appContext, locationAccess)
    val uiSounds: UiSounds = SoundPoolUiSounds(appContext)
    val settingsLauncher = SettingsLauncher(appContext)
    val deviceInfo = DeviceInfo(appContext)

    /** Wallpaper media found on the device; scanned off the main thread when the home starts and when Customize opens. */
    val mediaCatalog: MediaCatalog = FileMediaCatalog(appContext)
    val playlistStateStore: PlaylistStateStore = SharedPrefsPlaylistStateStore(appContext)
    val mediaThumbnails: MediaThumbnails = MediaThumbnailLoader()

    val redirectPolicy = RedirectPolicy(SharedPrefsGuardStateStore(appContext), clock = System::currentTimeMillis, uptime = SystemClock::elapsedRealtime)
    val homeStarter = HomeStarter(appContext)
    val stockHomeResolver = StockHomeResolver(appContext)
    val stockLauncherOpener = StockLauncherOpener(appContext, stockHomeResolver, redirectPolicy)
    val accessibilityStatus = AccessibilityStatusReader(appContext)
    val watcherPermissions = WatcherPermissions(appContext)
    val foregroundWatcher = ForegroundWatcherController(appContext, watcherPermissions)

    private val httpTransport = HttpUrlConnectionTransport()
    val weatherRepository: WeatherRepository = CachedWeatherRepository(
        remote = OpenMeteoWeatherRemote(httpTransport),
        cache = FileWeatherCache(appContext.noBackupFilesDir),
    )
    val placeSearch: PlaceSearch = OpenMeteoPlaceSearch(httpTransport)

    /** What the screensaver draws from, live. Cold: each collector (the in-app overlay, the DreamService) gets its own clock and weather. */
    fun screensaverStates(): Flow<ScreensaverState> {
        val ticks = ScreensaverStates.minuteTicks()
        return ScreensaverStates.flow(
            config = configStore.config,
            ticks = ticks,
            library = mediaCatalog.library,
            weather = ScreensaverStates.weather(configStore.config, weatherRepository, ticks),
        )
    }

    val homeViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                catalog = appCatalog,
                ticks = ClockTicker.ticks(),
                ownPackage = appContext.packageName,
                monitor = networkMonitor,
                configStore = configStore,
                weatherRepository = weatherRepository,
                placeSearch = placeSearch,
                sounds = uiSounds,
            ) as T
    }
}
