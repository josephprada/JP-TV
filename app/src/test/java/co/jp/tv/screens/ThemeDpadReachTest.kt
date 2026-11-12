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
import co.jp.tv.themes.ThemeRegistry
import org.junit.Test

/**
 * Every theme must let a D-pad-only remote reach the settings control from the app list and come
 * back. The settings control is a clickable that takes focus; the list root is not clickable.
 * Every theme must also reach its themes control, and come back from it.
 */
@OptIn(ExperimentalTestApi::class)
class ThemeDpadReachTest : ScreenshotTest() {
    private fun focusedIsClickable(): Boolean {
        val nodes = rule.onAllNodes(isFocused()).fetchSemanticsNodes()
        return nodes.any { (it.config as SemanticsConfiguration).getOrNull(SemanticsActions.OnClick) != null }
    }

    private fun focusedIsThemesControl(): Boolean {
        val nodes = rule.onAllNodes(isFocused()).fetchSemanticsNodes()
        return nodes.any { node ->
            val config = node.config as SemanticsConfiguration
            val labels = config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() +
                config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
            labels.any { it.equals("Temas", ignoreCase = true) }
        }
    }

    /** [toThemes] are the D-pad presses from the app list to the themes control; [back] returns to the list. */
    private fun reachThemes(themeId: String, toThemes: List<Key>, back: List<Key>) {
        shoot("dpad-$themeId-themes", toThemes) {
            ThemeRegistry.byId(themeId).Home(ScreenFixtures.homeState(themeId), ScreenFixtures.actions)
        }
        check(focusedIsThemesControl()) { "$themeId: $toThemes did not reach the themes control" }
        back.forEach { key ->
            rule.onRoot().performKeyInput { pressKey(key) }
            rule.mainClock.advanceTimeBy(400)
        }
        check(!focusedIsClickable()) { "$themeId: $back did not return to the app list from the themes control" }
    }

    private fun reach(themeId: String, toControl: Key, back: Key) {
        shoot("dpad-$themeId", listOf(toControl)) {
            ThemeRegistry.byId(themeId).Home(ScreenFixtures.homeState(themeId), ScreenFixtures.actions)
        }
        check(focusedIsClickable()) { "$themeId: $toControl did not reach the settings control" }
        rule.onRoot().performKeyInput { pressKey(back) }
        rule.mainClock.advanceTimeBy(400)
        check(!focusedIsClickable()) { "$themeId: $back did not return to the app list" }
    }

    @Test fun jptv() = reach("jptv", Key.DirectionRight, Key.DirectionLeft)
    @Test fun pixel() = reach("pixel", Key.DirectionUp, Key.DirectionDown)
    @Test fun terminal() = reach("terminal", Key.DirectionLeft, Key.DirectionDown)
    @Test fun cartelera() = reach("cartelera", Key.DirectionDown, Key.DirectionUp)
    @Test fun zen() = reach("zen", Key.DirectionRight, Key.DirectionLeft)
    @Test fun hud() = reach("hud", Key.DirectionUp, Key.DirectionLeft)
    @Test fun vinilo() = reach("vinilo", Key.DirectionUp, Key.DirectionDown)
    @Test fun lofi() = reach("lofi", Key.DirectionRight, Key.DirectionLeft)
    @Test fun gotham() = reach("gotham", Key.DirectionUp, Key.DirectionDown)
    @Test fun multiverso() = reach("multiverso", Key.DirectionUp, Key.DirectionDown)
    @Test fun cyberpunk() = reach("cyberpunk", Key.DirectionUp, Key.DirectionDown)
    @Test fun oceano() = reach("oceano", Key.DirectionUp, Key.DirectionDown)

    private val Up = Key.DirectionUp
    private val Down = Key.DirectionDown
    private val Left = Key.DirectionLeft
    private val Right = Key.DirectionRight

    @Test fun jptvThemes() = reachThemes("jptv", listOf(Right, Right, Right), listOf(Left, Left, Left))
    @Test fun pixelThemes() = reachThemes("pixel", listOf(Up, Right), listOf(Down))
    @Test fun terminalThemes() = reachThemes("terminal", listOf(Left, Right), listOf(Down))
    @Test fun carteleraThemes() = reachThemes("cartelera", listOf(Down, Right), listOf(Up))
    @Test fun zenThemes() = reachThemes("zen", listOf(Right, Right), listOf(Down))
    @Test fun hudThemes() = reachThemes("hud", listOf(Up, Down), listOf(Down))
    @Test fun viniloThemes() = reachThemes("vinilo", listOf(Up, Right), listOf(Down))
    @Test fun lofiThemes() = reachThemes("lofi", listOf(Right, Right), listOf(Up))
    @Test fun gothamThemes() = reachThemes("gotham", listOf(Up, Right), listOf(Down))
    @Test fun multiversoThemes() = reachThemes("multiverso", listOf(Up, Right), listOf(Down))
    @Test fun cyberpunkThemes() = reachThemes("cyberpunk", listOf(Up, Right), listOf(Down))
    @Test fun oceanoThemes() = reachThemes("oceano", listOf(Up, Right), listOf(Down))
}
