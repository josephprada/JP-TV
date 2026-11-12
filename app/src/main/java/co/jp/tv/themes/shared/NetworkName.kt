package co.jp.tv.themes.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.jp.tv.R
import co.jp.tv.network.domain.NetworkStatus

/** Human network name for a theme's top bar: the SSID, else the transport, else the offline text. */
@Composable
fun networkName(network: NetworkStatus): String = when {
    network.state == NetworkStatus.State.DISCONNECTED -> stringResource(R.string.network_offline)
    network.ssid != null -> network.ssid
    network.transport == NetworkStatus.Transport.WIFI -> stringResource(R.string.network_wifi)
    network.transport == NetworkStatus.Transport.ETHERNET -> stringResource(R.string.network_ethernet)
    else -> stringResource(R.string.network_generic)
}
