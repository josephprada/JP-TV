package co.jp.tv.screensaver.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SaverKeyGateTest {
    @Test
    fun `keys pass through when the saver is not showing`() {
        val gate = SaverKeyGate()
        assertEquals(SaverKeyGate.Verdict.PASS, gate.verdict(showing = false, isDown = true, keyCode = 23, repeat = 0))
        assertEquals(SaverKeyGate.Verdict.PASS, gate.verdict(showing = false, isDown = false, keyCode = 23, repeat = 0))
    }

    @Test
    fun `the first press dismisses the saver and the key does nothing else`() {
        val gate = SaverKeyGate()
        assertEquals(SaverKeyGate.Verdict.DISMISS, gate.verdict(showing = true, isDown = true, keyCode = 23, repeat = 0))
        // The release of that same press arrives after the saver is gone and must not reach the app either.
        assertEquals(SaverKeyGate.Verdict.CONSUME, gate.verdict(showing = false, isDown = false, keyCode = 23, repeat = 0))
        // Only once: the next release is a normal one.
        assertEquals(SaverKeyGate.Verdict.PASS, gate.verdict(showing = false, isDown = false, keyCode = 23, repeat = 0))
    }

    @Test
    fun `a held key keeps being consumed while the saver shows`() {
        val gate = SaverKeyGate()
        gate.verdict(showing = true, isDown = true, keyCode = 20, repeat = 0)
        assertEquals(SaverKeyGate.Verdict.CONSUME, gate.verdict(showing = true, isDown = true, keyCode = 20, repeat = 3))
        assertEquals(SaverKeyGate.Verdict.CONSUME, gate.verdict(showing = true, isDown = false, keyCode = 20, repeat = 0))
    }

    @Test
    fun `the release of an older key is not swallowed`() {
        val gate = SaverKeyGate()
        gate.verdict(showing = true, isDown = true, keyCode = 23, repeat = 0)
        assertEquals(SaverKeyGate.Verdict.PASS, gate.verdict(showing = false, isDown = false, keyCode = 19, repeat = 0))
    }
}
