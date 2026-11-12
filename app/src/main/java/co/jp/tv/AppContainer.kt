package co.jp.tv

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.jp.tv.apps.data.AppIconLoader
import co.jp.tv.apps.data.FileAppListStore
import co.jp.tv.apps.data.LaunchAnchor
import co.jp.tv.apps.data.PackageManagerAppCatalog
import co.jp.tv.apps.domain.CachedAppCatalog
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.core.util.ClockTicker
import co.jp.tv.home.HomeViewModel
import co.jp.tv.network.data.ConnectivityNetworkMonitor
import co.jp.tv.network.data.LocationAccess
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.preferences.data.DataStoreConfigStore
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.settings.data.DeviceInfo
import co.jp.tv.settings.data.SettingsLauncher
import co.jp.tv.weather.data.FileWeatherCache
import co.jp.tv.weather.data.HttpUrlConnectionTransport
import co.jp.tv.weather.data.OpenMeteoPlaceSearch
import co.jp.tv.weather.data.OpenMeteoWeatherRemote
import co.jp.tv.weather.domain.CachedWeatherRepository
import co.jp.tv.weather.domain.PlaceSearch
import co.jp.tv.weather.domain.WeatherRepository

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
    val settingsLauncher = SettingsLauncher(appContext)
    val deviceInfo = DeviceInfo(appContext)

    private val httpTransport = HttpUrlConnectionTransport()
    val weatherRepository: WeatherRepository = CachedWeatherRepository(
        remote = OpenMeteoWeatherRemote(httpTransport),
        cache = FileWeatherCache(appContext.noBackupFilesDir),
    )
    val placeSearch: PlaceSearch = OpenMeteoPlaceSearch(httpTransport)

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
            ) as T
    }
}
