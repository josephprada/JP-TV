package co.jp.tv.screens

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
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
 */
@OptIn(ExperimentalTestApi::class)
class ThemeDpadReachTest : ScreenshotTest() {
    private fun focusedIsClickable(): Boolean {
        val nodes = rule.onAllNodes(isFocused()).fetchSemanticsNodes()
        return nodes.any { (it.config as SemanticsConfiguration).getOrNull(SemanticsActions.OnClick) != null }
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
    @Test fun hud() = reach("hud", Key.DirectionUp, Key.DirectionDown)
    @Test fun vinilo() = reach("vinilo", Key.DirectionUp, Key.DirectionDown)
    @Test fun lofi() = reach("lofi", Key.DirectionRight, Key.DirectionLeft)
    @Test fun gotham() = reach("gotham", Key.DirectionUp, Key.DirectionDown)
    @Test fun multiverso() = reach("multiverso", Key.DirectionUp, Key.DirectionDown)
    @Test fun cyberpunk() = reach("cyberpunk", Key.DirectionUp, Key.DirectionDown)
    @Test fun oceano() = reach("oceano", Key.DirectionUp, Key.DirectionDown)
}
