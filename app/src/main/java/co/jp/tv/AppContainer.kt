package co.jp.tv

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.jp.tv.apps.data.AppIconLoader
import co.jp.tv.apps.data.PackageManagerAppCatalog
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.core.util.ClockTicker
import co.jp.tv.home.HomeViewModel
import co.jp.tv.network.data.ConnectivityNetworkMonitor
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.settings.data.DeviceInfo
import co.jp.tv.settings.data.SettingsLauncher

/**
 * Manual dependency holder. The only place that creates adapters and wires them to ports.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val appCatalog: AppCatalog = PackageManagerAppCatalog(appContext)
    val appIconLoader = AppIconLoader(appContext)
    val networkMonitor: NetworkMonitor = ConnectivityNetworkMonitor(appContext)
    val settingsLauncher = SettingsLauncher(appContext)
    val deviceInfo = DeviceInfo(appContext)

    val homeViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                catalog = appCatalog,
                ticks = ClockTicker.ticks(),
                ownPackage = appContext.packageName,
                monitor = networkMonitor,
            ) as T
    }
}
