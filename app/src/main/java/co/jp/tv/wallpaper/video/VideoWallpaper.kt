package co.jp.tv.wallpaper.video

import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.wallpaper.Wallpaper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Looped, muted video wallpaper from `/sdcard/JPTV/loop.mp4`.
 * Shows a mono placeholder when the file is missing.
 */
object VideoWallpaper : Wallpaper {
    override val id = ID
    override val displayName = "Mi video"

    const val ID = "video"
    const val EXTERNAL_FILE_NAME = "loop.mp4"

    fun externalFile(): File =
        File(Environment.getExternalStorageDirectory(), "JPTV/$EXTERNAL_FILE_NAME")

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val context = LocalContext.current
        val file = remember { externalFile() }
        var exists by remember { mutableStateOf(false) }
        LaunchedEffect(file) { exists = withContext(Dispatchers.IO) { file.isFile } }

        Box(modifier.fillMaxSize().background(JpColors.Ink), contentAlignment = Alignment.Center) {
            if (!exists) {
                BasicText(
                    text = "TU VIDEO · /sdcard/JPTV/loop.mp4",
                    style = TextStyle(
                        color = JpColors.Faint,
                        fontSize = 13.sp,
                        fontFamily = JpFonts.Mono,
                        letterSpacing = 0.18.em,
                    ),
                )
            } else {
                val player = remember(file.absolutePath) {
                    ExoPlayer.Builder(context).build().apply {
                        repeatMode = Player.REPEAT_MODE_ONE
                        volume = 0f
                        setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                        prepare()
                        playWhenReady = !paused
                    }
                }
                LaunchedEffect(paused) { player.playWhenReady = !paused }
                DisposableEffect(player) { onDispose { player.release() } }
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            this.player = player
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
