package co.jp.tv.screens

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.performKeyInput
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.util.LocalPaused
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders a composable at the V_BOX14's real size: 1280x720 px at 213 dpi (about 961x540 dp).
 * Animations are paused and the clock is frozen after the entrance animations finish.
 * Output goes to `screens/<name>.png` (see `make screens`).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w962dp-h541dp-213dpi", application = android.app.Application::class)
abstract class ScreenshotTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun composeViews(view: View): List<View> =
        if (view.javaClass.name.endsWith("AndroidComposeView")) {
            listOf(view)
        } else {
            (view as? ViewGroup)?.let { g -> (0 until g.childCount).flatMap { composeViews(g.getChildAt(it)) } }.orEmpty()
        }

    /** [keys] are D-pad presses sent after the entrance animations, to render focus states. */
    protected fun shoot(name: String, keys: List<Key> = emptyList(), content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(
                LocalPaused provides true,
                LocalAppIcons provides ScreenFixtures.icons,
            ) {
                Box(Modifier.fillMaxSize().background(JpColors.Ink)) { content() }
            }
        }
        // Robolectric windows start unfocused, and Compose only draws focus in a focused window.
        rule.runOnUiThread {
            val decor = rule.activity.window.decorView
            decor.dispatchWindowFocusChanged(true)
            // A TV is driven by the D-pad, never in touch mode; Compose only focuses clickables outside it.
            runCatching {
                val root = View::class.java.getMethod("getViewRootImpl").invoke(decor)!!
                root.javaClass.getDeclaredMethod("ensureTouchMode", Boolean::class.javaPrimitiveType)
                    .apply { isAccessible = true }.invoke(root, false)
            }
            composeViews(decor).firstOrNull()?.apply {
                // Robolectric stays in touch mode; a TV is driven by the D-pad, never in touch mode.
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
        rule.mainClock.advanceTimeBy(100)
        rule.mainClock.advanceTimeBy(100)
        // Past the longest entrance animation (riseIn stagger + 600 ms).
        rule.mainClock.advanceTimeBy(3_000)
        rule.waitForIdle()
        keys.forEach { key ->
            rule.onRoot().performKeyInput { pressKey(key) }
            rule.mainClock.advanceTimeBy(400)
        }
        val dir = File(System.getProperty("screens.dir") ?: "../screens").apply { mkdirs() }
        rule.onRoot().captureRoboImage(File(dir, "$name.png").path)
    }
}
