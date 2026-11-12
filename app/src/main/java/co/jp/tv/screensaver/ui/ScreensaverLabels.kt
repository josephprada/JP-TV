package co.jp.tv.screensaver.ui

import androidx.annotation.StringRes
import co.jp.tv.R
import co.jp.tv.screensaver.domain.ScreensaverMode

@StringRes
fun ScreensaverMode.labelRes(): Int = when (this) {
    ScreensaverMode.PHOTOS -> R.string.screensaver_mode_photos
    ScreensaverMode.CLOCK -> R.string.screensaver_mode_clock
    ScreensaverMode.LIVE -> R.string.screensaver_mode_live
    ScreensaverMode.AMBIENT -> R.string.screensaver_mode_ambient
    ScreensaverMode.RANDOM -> R.string.screensaver_mode_random
}

@StringRes
fun ScreensaverMode.descriptionRes(): Int = when (this) {
    ScreensaverMode.PHOTOS -> R.string.screensaver_desc_photos
    ScreensaverMode.CLOCK -> R.string.screensaver_desc_clock
    ScreensaverMode.LIVE -> R.string.screensaver_desc_live
    ScreensaverMode.AMBIENT -> R.string.screensaver_desc_ambient
    ScreensaverMode.RANDOM -> R.string.screensaver_desc_random
}
