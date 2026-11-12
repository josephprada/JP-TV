package co.jp.tv.core.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LaunchReturnGateTest {
    @Test
    fun `a resume without a launch plays nothing`() {
        val gate = LaunchReturnGate()
        gate.onPaused()
        assertFalse(gate.onResumed())
    }

    @Test
    fun `resuming after a launch that paused the home plays the return once`() {
        val gate = LaunchReturnGate()
        gate.launched()
        gate.onPaused()
        assertTrue(gate.onResumed())
        gate.onPaused()
        assertFalse(gate.onResumed())
    }

    @Test
    fun `a launch that never paused the home is not a return`() {
        val gate = LaunchReturnGate()
        gate.launched()
        assertFalse(gate.hasLeft)
        assertFalse(gate.onResumed())
    }

    @Test
    fun `a pause before the launch does not count as leaving`() {
        val gate = LaunchReturnGate()
        gate.onPaused()
        gate.launched()
        assertFalse(gate.onResumed())
    }

    @Test
    fun `cancel disarms the gate`() {
        val gate = LaunchReturnGate()
        gate.launched()
        gate.cancel()
        gate.onPaused()
        assertFalse(gate.onResumed())
    }
}
