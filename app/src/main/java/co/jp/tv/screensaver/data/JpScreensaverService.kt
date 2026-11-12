package co.jp.tv.screensaver.data

import android.service.dreams.DreamService
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import co.jp.tv.JpTvApp
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.screensaver.ui.ScreensaverHost

/**
 * The system screensaver ("Protector de pantalla" in Android Settings > Display). It hosts the same
 * [ScreensaverHost] the in-app overlay shows, in a [ComposeView] with the lifecycle and saved-state owners a
 * DreamService does not have. Any key or touch ends the dream (it is not interactive); the player of the
 * "Live" mode is released as soon as the dream stops or the window goes away.
 */
class JpScreensaverService : DreamService(), LifecycleOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)
    private val dreaming = mutableStateOf(false)
    private var content: ComposeView? = null

    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedState.performRestore(null)
        registry.currentState = Lifecycle.State.CREATED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = true
        val container = (application as JpTvApp).container
        val view = ComposeView(this).apply {
            setContent {
                // Black until the first reading of the config, so a wrong mode never flashes.
                val state by remember { container.screensaverStates() }.collectAsState(initial = null)
                LaunchedEffect(Unit) { container.mediaCatalog.refresh() }
                // "Paused" while the dream is not on screen: the player is released and nothing animates.
                CompositionLocalProvider(LocalPaused provides !dreaming.value) {
                    state?.let { ScreensaverHost(it) }
                }
            }
        }
        // Compose finds its owners by walking up from the view, so they go on the window's decor view.
        window.decorView.setViewTreeLifecycleOwner(this)
        window.decorView.setViewTreeSavedStateRegistryOwner(this)
        setContentView(view)
        content = view
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        registry.currentState = Lifecycle.State.RESUMED
        dreaming.value = true
    }

    override fun onDreamingStopped() {
        dreaming.value = false
        release()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        release()
        registry.currentState = Lifecycle.State.DESTROYED
        super.onDetachedFromWindow()
    }

    /** Ends the composition (and with it the player and every animation) now, not when the system gets around to it. */
    private fun release() {
        content?.disposeComposition()
        content = null
        if (registry.currentState.isAtLeast(Lifecycle.State.STARTED)) registry.currentState = Lifecycle.State.CREATED
    }
}
