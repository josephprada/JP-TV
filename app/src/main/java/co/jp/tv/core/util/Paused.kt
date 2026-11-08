package co.jp.tv.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

/** True while the activity is stopped; every infinite animation must idle when this is true. */
val LocalPaused = compositionLocalOf { false }

@Composable
fun rememberPausedByLifecycle(): Boolean {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    return !state.isAtLeast(Lifecycle.State.STARTED)
}
