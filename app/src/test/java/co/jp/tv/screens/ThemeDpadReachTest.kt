package co.jp.tv.screens

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.themes.shared.HomeActions
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Test

/**
 * Every theme must let a D-pad-only remote walk the header controls in order, network, settings,
 * themes, starting from the app list, and come back to the list. Each control is a clickable that
 * takes focus (the list root is not clickable); OK on the network control opens the network panel.
 */
@OptIn(ExperimentalTestApi::class)
class ThemeDpadReachTest : ScreenshotTest() {
    private fun focusedLabels(): List<String> =
        rule.onAllNodes(isFocused()).fetchSemanticsNodes().flatMap { node ->
            val config = node.config as SemanticsConfiguration
            val clickable = config.getOrNull(SemanticsActions.OnClick) != null
            if (!clickable) emptyList()
            else config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() +
                config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
        }

    private fun focusedIs(label: String) = focusedLabels().any { it.equals(label, ignoreCase = true) }

    private fun focusedIsClickable(): Boolean =
        rule.onAllNodes(isFocused()).fetchSemanticsNodes()
            .any { (it.config as SemanticsConfiguration).getOrNull(SemanticsActions.OnClick) != null }

    private fun press(keys: List<Key>) = keys.forEach { key ->
        rule.onRoot().performKeyInput { pressKey(key) }
        rule.mainClock.advanceTimeBy(400)
    }

    private val opened = mutableListOf<String>()
    private val recording = object : HomeActions {
        override fun launch(app: TvApp) = Unit
        override fun focus(index: Int) = Unit
        override fun openNetwork() { opened += "network" }
        override fun openSettings() { opened += "settings" }
        override fun openThemes() { opened += "themes" }
        override fun openCustomize() = Unit
        override fun openEditApps(focus: TvApp?) = Unit
    }

    /**
     * [toNetwork] are the presses from the app list to the network control, [toSettings] and [toThemes]
     * the presses to each following control, [back] the presses from the themes control to the list.
     */
    private fun reach(
        themeId: String,
        toNetwork: List<Key>,
        toSettings: List<Key>,
        toThemes: List<Key>,
        back: List<Key>,
    ) {
        shoot("dpad-$themeId-network", toNetwork) {
            ThemeRegistry.byId(themeId).Home(ScreenFixtures.homeState(themeId), recording)
        }
        check(focusedIs("Ver detalles de red")) { "$themeId: $toNetwork did not reach the network control" }
        // The first key of a run is drawn one frame late; settle, then keep the picture of the focused control.
        rule.mainClock.advanceTimeBy(400)
        rule.onRoot().captureRoboImage(File(System.getProperty("screens.dir") ?: "../screens", "dpad-$themeId-network.png").path)
        press(listOf(Key.DirectionCenter))
        check(opened == listOf("network")) { "$themeId: OK on the network control did not open the network panel: $opened" }
        press(toSettings)
        check(focusedIs("Ajustes")) { "$themeId: $toSettings did not move from network to the settings control" }
        press(toThemes)
        check(focusedIs("Temas")) { "$themeId: $toThemes did not move from settings to the themes control" }
        press(back)
        check(!focusedIsClickable()) { "$themeId: $back did not return to the app list from the themes control" }
    }

    private val Up = Key.DirectionUp
    private val Down = Key.DirectionDown
    private val Left = Key.DirectionLeft
    private val Right = Key.DirectionRight

    // jptv has no routing: its pills take focus natively, left to right, and Left walks back.
    @Test fun jptv() = reach("jptv", listOf(Right), listOf(Right), listOf(Right), listOf(Left, Left, Left))
    @Test fun pixel() = reach("pixel", listOf(Up), listOf(Right), listOf(Right), listOf(Down))
    @Test fun terminal() = reach("terminal", listOf(Left), listOf(Right), listOf(Right), listOf(Down))
    @Test fun cartelera() = reach("cartelera", listOf(Down), listOf(Right), listOf(Right), listOf(Up))
    @Test fun zen() = reach("zen", listOf(Right), listOf(Right), listOf(Right), listOf(Down))
    @Test fun hud() = reach("hud", listOf(Up), listOf(Right), listOf(Down), listOf(Down))
    @Test fun vinilo() = reach("vinilo", listOf(Up), listOf(Right), listOf(Right), listOf(Down))
    @Test fun lofi() = reach("lofi", listOf(Right), listOf(Right), listOf(Right), listOf(Up))
    @Test fun gotham() = reach("gotham", listOf(Up), listOf(Right), listOf(Right), listOf(Down))
    @Test fun multiverso() = reach("multiverso", listOf(Up), listOf(Right), listOf(Right), listOf(Down))
    @Test fun cyberpunk() = reach("cyberpunk", listOf(Up), listOf(Right), listOf(Right), listOf(Down))
    @Test fun oceano() = reach("oceano", listOf(Up), listOf(Right), listOf(Right), listOf(Down))
}
