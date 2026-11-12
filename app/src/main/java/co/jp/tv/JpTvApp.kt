package co.jp.tv

import android.app.Application
import co.jp.tv.core.design.FontPreloader
import co.jp.tv.core.util.StartupTrace
import co.jp.tv.homeoverride.data.CrashRecorder
import co.jp.tv.preferences.domain.LauncherConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JpTvApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        StartupTrace.init(this)
        StartupTrace.mark("Application.onCreate")
        container = AppContainer(this)
        CrashRecorder.install(container.redirectPolicy)
        warmUp()
    }

    /** Starts the slow, main-thread-free work before the activity needs it: the DataStore read and the font decode. */
    private fun warmUp() {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val config = container.configStore.config.first()
            StartupTrace.mark("warmUp: config read")
            // Decode the active theme's clips now (SoundPool loads on its own thread) so the first key press is heard.
            withContext(Dispatchers.Main) { container.uiSounds.useSet(config?.themeId ?: LauncherConfig.DEFAULT_THEME_ID) }
            FontPreloader.preload(this@JpTvApp, config?.titleFontId ?: LauncherConfig.Default.titleFontId)
            StartupTrace.mark("warmUp: fonts decoded")
        }
    }
}
