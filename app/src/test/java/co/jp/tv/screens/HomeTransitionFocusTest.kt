package co.jp.tv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isFocused
import co.jp.tv.core.design.HomeTransition
import co.jp.tv.core.design.HomeTransitionHost
import co.jp.tv.themes.ThemeRegistry
import kotlinx.coroutines.launch
import org.junit.Test

/**
 * Launching an app and coming back must leave D-pad focus where it was: the home stays composed
 * while the activity is stopped, so the return entrance must not touch focus (re-requesting it on the
 * home container moved it off the settings control).
 */
@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
class HomeTransitionFocusTest : ScreenshotTest() {
    private var step by mutableIntStateOf(0)
    private val transition = HomeTransition(lowEnd = false)

    private fun focusedIsClickable(): Boolean =
        rule.onAllNodes(isFocused()).fetchSemanticsNodes()
            .any { (it.config as SemanticsConfiguration).getOrNull(SemanticsActions.OnClick) != null }

    /** The compose rule does not flush state written from the test thread while its clock is manual. */
    private fun goTo(next: Int) = rule.runOnUiThread {
        step = next
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun focusSurvivesLaunchAndReturn() {
        shoot("transition-focus", listOf(Key.DirectionRight)) {
            val homeFocus = remember { FocusRequester() }
            val scope = rememberCoroutineScope()
            LaunchedEffect(step) {
                when (step) {
                    1 -> scope.launch { transition.launchApp(1280, 720) { } }
                    2 -> transition.onPausedChanged(paused = true)
                    3 -> transition.onPausedChanged(paused = false)
                }
            }
            HomeTransitionHost(transition, Color(0xFFE63329), lowEnd = false) {
                Box(Modifier.fillMaxSize().focusRequester(homeFocus).focusRestorer()) {
                    ThemeRegistry.byId("zen").Home(ScreenFixtures.homeState("zen"), ScreenFixtures.actions)
                }
            }
        }
        check(focusedIsClickable()) { "setup: the settings control should hold focus" }

        goTo(1)
        rule.mainClock.advanceTimeBy(400)
        check(transition.launchProgress > 0.99f) { "the exit should have finished: ${transition.launchProgress}" }

        goTo(2)
        rule.mainClock.advanceTimeBy(1_500)
        check(transition.launchProgress > 0.99f) { "the home stays hidden while the app is open" }

        goTo(3)
        rule.mainClock.advanceTimeBy(HomeTransition.ReturnMs + 100L)
        check(transition.launchProgress == 0f && transition.returnProgress == 1f) { "the home should be fully back: out=${transition.launchProgress} back=${transition.returnProgress}" }
        check(focusedIsClickable()) { "focus should be back on the settings control after returning" }
    }

    @Test
    fun aLaunchThatNeverLeavesBringsTheHomeBack() {
        shoot("transition-recover", emptyList()) {
            val scope = rememberCoroutineScope()
            LaunchedEffect(step) { if (step == 1) scope.launch { transition.launchApp(1280, 720) { } } }
            HomeTransitionHost(transition, Color(0xFFE63329), lowEnd = false) {
                ThemeRegistry.byId("zen").Home(ScreenFixtures.homeState("zen"), ScreenFixtures.actions)
            }
        }
        goTo(1)
        rule.mainClock.advanceTimeBy(400)
        check(transition.launchProgress > 0.99f) { "exit not done: ${transition.launchProgress}" }
        rule.mainClock.advanceTimeBy(1_600)
        check(transition.launchProgress == 0f) { "the home should recover when the launch never paused it" }
    }
}
