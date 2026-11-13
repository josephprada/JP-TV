package co.jp.tv.core.util

/** Process-wide flag: true while MainActivity is resumed, so late boot starts do not launch it twice. */
object MainVisibility {
    @Volatile
    var resumed = false
}
