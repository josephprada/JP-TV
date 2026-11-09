package co.jp.tv.preferences.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import co.jp.tv.wallpaper.image.StaticImageWallpaper
import co.jp.tv.wallpaper.video.VideoWallpaper

/** Runtime permission a wallpaper needs to read `/sdcard/JPTV`, or null when none applies. */
internal fun mediaPermissionFor(wallpaperId: String, sdk: Int = Build.VERSION.SDK_INT): String? = when {
    sdk < Build.VERSION_CODES.M -> null
    wallpaperId == StaticImageWallpaper.ID ->
        if (sdk >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    wallpaperId == VideoWallpaper.ID ->
        if (sdk >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_EXTERNAL_STORAGE
    else -> null
}

/** Returns a function that asks for the storage permission [wallpaperId] needs; [onResult] runs after the dialog. */
@Composable
fun rememberMediaPermissionRequester(onResult: () -> Unit): (String) -> Unit {
    val context = LocalContext.current
    val latest = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { latest.value() }
    return { wallpaperId ->
        val permission = mediaPermissionFor(wallpaperId)
        if (permission != null &&
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(permission)
        }
    }
}
