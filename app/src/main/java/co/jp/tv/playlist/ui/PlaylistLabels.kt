package co.jp.tv.playlist.ui

import androidx.annotation.StringRes
import co.jp.tv.R
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.RotationInterval

/** The Spanish chip label of each rotation interval. */
@StringRes
fun RotationInterval.labelRes(): Int = when (this) {
    RotationInterval.NEVER -> R.string.playlist_interval_never
    RotationInterval.MIN_5 -> R.string.playlist_interval_5
    RotationInterval.MIN_15 -> R.string.playlist_interval_15
    RotationInterval.MIN_30 -> R.string.playlist_interval_30
    RotationInterval.MIN_60 -> R.string.playlist_interval_60
    RotationInterval.ON_HOME_RETURN -> R.string.playlist_interval_home
}

@StringRes
fun PlaylistOrder.labelRes(): Int = when (this) {
    PlaylistOrder.SEQUENTIAL -> R.string.playlist_order_sequential
    PlaylistOrder.SHUFFLE -> R.string.playlist_order_shuffle
}
