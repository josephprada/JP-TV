package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RedirectPolicyTest {
    private class MemoryStore(var state: GuardState = GuardState()) : GuardStateStore {
        override fun load() = state
        override fun save(state: GuardState) { this.state = state }
    }

    private val store = MemoryStore()
    private var now = 1_000_000L
    private var uptime = 10 * 60_000L
    private val policy = RedirectPolicy(store, clock = { now }, uptime = { uptime })

    private fun advance(ms: Long) { now += ms }
    private fun decide(trigger: Trigger = Trigger.STOCK_WINDOW, enabled: Boolean = true) = policy.decide(enabled, trigger)
    private fun redirect() { policy.onRedirected() }

    @Test
    fun `redirects when enabled and nothing is wrong`() {
        assertEquals(Decision.Redirect, decide())
    }

    @Test
    fun `passes through when the toggle is off`() {
        assertEquals(Decision.PassThrough(Reason.DISABLED), decide(enabled = false))
    }

    @Test
    fun `a second stock window within the debounce is ignored`() {
        redirect()
        advance(RedirectPolicy.DEBOUNCE_MS - 1)
        assertEquals(Decision.PassThrough(Reason.DEBOUNCED), decide())
        advance(1)
        assertEquals(Decision.Redirect, decide())
    }

    @Test
    fun `the home key and boot are never debounced`() {
        redirect()
        assertEquals(Decision.Redirect, decide(Trigger.HOME_KEY))
        assertEquals(Decision.Redirect, decide(Trigger.BOOT))
    }

    @Test
    fun `stock windows right after boot are left alone until the system settles`() {
        uptime = RedirectPolicy.MIN_UPTIME_MS - 1
        assertEquals(Decision.PassThrough(Reason.BOOTING), decide(Trigger.STOCK_WINDOW))
        assertEquals(Decision.Redirect, decide(Trigger.BOOT))
        assertEquals(Decision.Redirect, decide(Trigger.HOME_KEY))
    }

    @Test
    fun `three crashes in ten minutes stop the override`() {
        repeat(2) { policy.onCrash(); advance(60_000) }
        assertEquals(Decision.Redirect, decide())
        policy.onCrash()
        advance(1_000)
        assertEquals(Decision.PassThrough(Reason.TOO_MANY_FAILURES), decide())
        assertEquals(Decision.PassThrough(Reason.TOO_MANY_FAILURES), decide(Trigger.HOME_KEY))
        assertEquals(Decision.PassThrough(Reason.TOO_MANY_FAILURES), decide(Trigger.BOOT))
    }

    @Test
    fun `crashes older than the window no longer count`() {
        repeat(3) { policy.onCrash() }
        advance(RedirectPolicy.FAILURE_WINDOW_MS + 1)
        assertEquals(Decision.Redirect, decide())
    }

    @Test
    fun `a redirect that never draws counts as a failure after five seconds`() {
        redirect()
        advance(RedirectPolicy.DRAW_TIMEOUT_MS - 1)
        assertEquals(false, policy.evaluatePending())
        advance(1)
        assertEquals(true, policy.evaluatePending())
        assertEquals(1, store.state.troubleAt.size)
        assertNull(store.state.pendingSince)
    }

    @Test
    fun `a failure is counted only once`() {
        redirect()
        advance(RedirectPolicy.DRAW_TIMEOUT_MS)
        policy.evaluatePending()
        assertEquals(false, policy.evaluatePending())
        assertEquals(1, store.state.troubleAt.size)
    }

    @Test
    fun `drawing in time clears the pending redirect`() {
        redirect()
        advance(2_000)
        policy.onHomeDrawn()
        advance(RedirectPolicy.DRAW_TIMEOUT_MS)
        assertEquals(false, policy.evaluatePending())
        assertTrue(store.state.troubleAt.isEmpty())
    }

    @Test
    fun `three redirects that never draw stop the override`() {
        repeat(3) {
            redirect()
            advance(RedirectPolicy.DRAW_TIMEOUT_MS)
            policy.evaluatePending()
            advance(RedirectPolicy.DEBOUNCE_MS)
        }
        assertEquals(Decision.PassThrough(Reason.TOO_MANY_FAILURES), decide())
    }

    @Test
    fun `deciding settles an overdue redirect first`() {
        repeat(2) { policy.onCrash() }
        redirect()
        advance(RedirectPolicy.DRAW_TIMEOUT_MS + RedirectPolicy.DEBOUNCE_MS)
        assertEquals(Decision.PassThrough(Reason.TOO_MANY_FAILURES), decide())
    }

    @Test
    fun `a crash replaces the pending redirect instead of counting twice`() {
        redirect()
        policy.onCrash()
        advance(RedirectPolicy.DRAW_TIMEOUT_MS)
        policy.evaluatePending()
        assertEquals(1, store.state.troubleAt.size)
    }

    @Test
    fun `opening the stock launcher on purpose pauses the override for ten minutes`() {
        policy.startGrace()
        assertEquals(Decision.PassThrough(Reason.GRACE), decide())
        assertEquals(Decision.PassThrough(Reason.GRACE), decide(Trigger.HOME_KEY))
        advance(RedirectPolicy.GRACE_MS - 1)
        assertEquals(Decision.PassThrough(Reason.GRACE), decide())
        advance(1)
        assertEquals(Decision.Redirect, decide())
    }

    @Test
    fun `the grace period survives a restart of the process`() {
        policy.startGrace()
        val reborn = RedirectPolicy(store, clock = { now }, uptime = { uptime })
        assertEquals(Decision.PassThrough(Reason.GRACE), reborn.decide(true, Trigger.STOCK_WINDOW))
    }

    @Test
    fun `a grace end absurdly far in the future is ignored`() {
        store.state = GuardState(graceUntil = now + 10 * RedirectPolicy.GRACE_MS)
        assertEquals(Decision.Redirect, decide())
    }

    @Test
    fun `a heartbeat with nothing pending writes nothing`() {
        val before = store.state
        policy.onHomeDrawn()
        assertEquals(before, store.state)
    }
}
