package co.jp.tv

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import androidx.core.content.ContextCompat
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
import java.util.concurrent.atomic.AtomicBoolean

class JpTvApp : Application() {
    /**
     * Built on first use. Its adapters open credential-encrypted storage (DataStore, SharedPreferences, files),
     * which does not exist yet while the process serves the direct-boot splash, so nothing touches it before the unlock.
     */
    val container: AppContainer by lazy {
        AppContainer(this).also { CrashRecorder.install(it.redirectPolicy) }
    }

    private val started = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        StartupTrace.init(this)
        StartupTrace.mark("Application.onCreate")
        if (isUserUnlocked()) {
            start()
        } else {
            // Booting: only the direct-boot splash can be running. Defer everything until the unlock.
            val onUnlocked = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    runCatching { unregisterReceiver(this) }
                    start()
                }
            }
            ContextCompat.registerReceiver(this, onUnlocked, IntentFilter(Intent.ACTION_USER_UNLOCKED), ContextCompat.RECEIVER_NOT_EXPORTED)
            // An unlock between the check and the registration would never be announced.
            if (isUserUnlocked()) onUnlocked.onReceive(this, Intent(Intent.ACTION_USER_UNLOCKED))
        }
    }

    private fun isUserUnlocked(): Boolean = getSystemService(UserManager::class.java)?.isUserUnlocked ?: true

    /** Builds the container, installs the crash recorder and warms the caches; runs once, only with the storage unlocked. */
    private fun start() {
        if (!started.compareAndSet(false, true)) return
        container
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
