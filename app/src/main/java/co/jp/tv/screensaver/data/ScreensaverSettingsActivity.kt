package co.jp.tv.screensaver.data

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.jp.tv.JpTvApp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.core.util.rememberPausedByLifecycle
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.preferences.ui.rememberMediaPermissionRequester
import co.jp.tv.screensaver.domain.SaverKeyGate
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.ui.ScreensaverHost
import co.jp.tv.screensaver.ui.ScreensaverSettingsScreen
import co.jp.tv.wallpaper.image.StaticImageWallpaper
import co.jp.tv.wallpaper.video.VideoWallpaper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

/**
 * Where the gear of Android's screensaver picker lands: the screensaver settings on their own, outside the
 * launcher. Same screen as Settings > Protector de pantalla, and the same preview (any key ends it).
 */
class ScreensaverSettingsActivity : ComponentActivity() {
    private val container get() = (application as JpTvApp).container
    private val previewing = mutableStateOf(false)
    private val keyGate = SaverKeyGate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val saved by container.configStore.config.collectAsStateWithLifecycle(initialValue = null)
            val config = saved ?: LauncherConfig.Default
            val scope = rememberCoroutineScope()
            val paused = rememberPausedByLifecycle()
            val requestMedia = rememberMediaPermissionRequester { scope.launch { container.mediaCatalog.refresh() } }
            LaunchedEffect(Unit) { container.mediaCatalog.refresh() }

            CompositionLocalProvider(LocalPaused provides paused) {
                Box(Modifier.fillMaxSize().background(JpColors.Ink)) {
                    if (saved != null) {
                        ScreensaverSettingsScreen(
                            settings = config.screensaver,
                            accent = Color(config.accent),
                            titleFontId = config.titleFontId,
                            onChange = { next ->
                                scope.launch { container.configStore.update { (it ?: LauncherConfig.Default).copy(screensaver = next) } }
                            },
                            onPreview = { previewing.value = true },
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
                    if (previewing.value) {
                        val state by remember { container.screensaverStates() }.collectAsStateWithLifecycle(initialValue = null)
                        state?.let { ScreensaverHost(it, Modifier.fillMaxSize()) }
                    }
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        when (keyGate.verdict(previewing.value, event.action == KeyEvent.ACTION_DOWN, event.keyCode, event.repeatCount)) {
            SaverKeyGate.Verdict.PASS -> super.dispatchKeyEvent(event)
            SaverKeyGate.Verdict.CONSUME -> true
            SaverKeyGate.Verdict.DISMISS -> {
                previewing.value = false
                true
            }
        }
}
