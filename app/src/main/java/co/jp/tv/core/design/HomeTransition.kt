package co.jp.tv.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import co.jp.tv.apps.domain.LaunchOrigin
import co.jp.tv.core.util.LaunchReturnGate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Launch and return animation of the home, shared by every theme.
 *
 * Launching: [launchApp] plays a short exit (the home zooms toward the focused tile and fades while an
 * accent glow blooms there; see [HomeTransitionHost]) and starts the activity before it ends, so the
 * app never waits more than [StartAtMs]. Returning: when the home resumes after such a launch,
 * [onPausedChanged] plays an entrance (fade, scale from 0.96, staggered rise through
 * [returnStagger]).
 *
 * Progress values are Compose state read only in draw and layer blocks, so animating does not recompose
 * a theme.
 */
@Stable
class HomeTransition(private val lowEnd: Boolean) {
    private val out = Animatable(0f)
    private val back = Animatable(1f)
    private val gate = LaunchReturnGate()
    private var launching = false
    private var originSource: OriginHolder? = null

    /** 0 while the home is shown, 1 once it is gone for a launch. */
    val launchProgress: Float get() = out.value

    /** 0 right after coming back, 1 once settled. */
    val returnProgress: Float get() = back.value

    /** Zoom/glow centre of the last launch as fractions of the window, and the tile size (px); 0 when unknown. */
    var pivotX = 0.5f
        private set
    var pivotY = 0.5f
        private set
    var tileWidth = 0f
        private set
    var tileHeight = 0f
        private set

    /** Whether the glow should be drawn; false on low-end boxes. */
    val drawsGlow: Boolean get() = !lowEnd

    /** A theme tells which tile is focused; read when the user presses OK. */
    internal fun registerOrigin(holder: OriginHolder) {
        originSource = holder
    }

    /**
     * Plays the exit and calls [start] with the tile bounds (null when the theme reports none) after
     * [StartAtMs]. Does nothing while a launch is already running.
     */
    suspend fun launchApp(windowWidth: Int, windowHeight: Int, start: (LaunchOrigin?) -> Unit) {
        if (launching) return
        launching = true
        val bounds = originSource?.bounds()
        if (bounds != null && windowWidth > 0 && windowHeight > 0) {
            pivotX = (bounds.center.x / windowWidth).coerceIn(0f, 1f)
            pivotY = (bounds.center.y / windowHeight).coerceIn(0f, 1f)
            tileWidth = bounds.width
            tileHeight = bounds.height
        } else {
            pivotX = 0.5f
            pivotY = 0.5f
            tileWidth = 0f
            tileHeight = 0f
        }
        gate.launched()
        val exitMs = if (lowEnd) LowEndExitMs else ExitMs
        coroutineScope {
            launch { out.animateTo(1f, tween(exitMs, easing = JpMotion.Entrance)) }
            delay(if (lowEnd) LowEndStartAtMs else StartAtMs)
            start(bounds?.let { LaunchOrigin(it.left.toInt(), it.top.toInt(), it.width.toInt(), it.height.toInt()) })
        }
        // The launch may not have happened (no intent, app refused): bring the home back.
        delay(RecoverAfterMs)
        if (!gate.hasLeft) {
            gate.cancel()
            out.animateTo(0f, tween(FocusRecoverMs, easing = JpMotion.Entrance))
            launching = false
        }
    }

    /**
     * Call when the activity pauses or resumes. The home stays composed while an app is open, so D-pad
     * focus is untouched; re-requesting it here would move it off the control that held it.
     */
    suspend fun onPausedChanged(paused: Boolean) {
        if (paused) {
            gate.onPaused()
            back.snapTo(1f)
            return
        }
        if (gate.onResumed()) {
            launching = false
            back.snapTo(0f)
            out.snapTo(0f)
            back.animateTo(1f, tween(ReturnMs, easing = JpMotion.Entrance))
        }
    }

    companion object {
        const val ExitMs = 260
        const val StartAtMs = 200L
        const val LowEndExitMs = 200
        const val LowEndStartAtMs = 150L
        const val ReturnMs = 420
        const val StaggerMs = 60
        private const val RecoverAfterMs = 1_200L
        private const val FocusRecoverMs = 200
    }
}

/** Holds the layout of the tile a theme marked with [launchOrigin]. */
internal class OriginHolder {
    var coordinates: LayoutCoordinates? = null

    fun bounds(): Rect? = coordinates?.takeIf { it.isAttached }?.boundsInWindow()
}

/** Null outside the real home (previews, screenshot tests): every hook then does nothing. */
val LocalHomeTransition = staticCompositionLocalOf<HomeTransition?> { null }
