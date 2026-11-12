package co.jp.tv.screensaver.domain

/**
 * Decides what happens to each key event while the in-app saver may be showing: any key dismisses it and
 * is consumed (it must do nothing else), and so is the release of that same press.
 */
class SaverKeyGate {
    enum class Verdict { PASS, CONSUME, DISMISS }

    private var swallowReleaseOf: Int? = null

    fun verdict(showing: Boolean, isDown: Boolean, keyCode: Int, repeat: Int): Verdict {
        if (showing) {
            if (isDown && repeat == 0) {
                swallowReleaseOf = keyCode
                return Verdict.DISMISS
            }
            return Verdict.CONSUME
        }
        if (!isDown && swallowReleaseOf == keyCode) {
            swallowReleaseOf = null
            return Verdict.CONSUME
        }
        return Verdict.PASS
    }
}
