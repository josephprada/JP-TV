package co.jp.tv.inputs.domain

import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.preferences.domain.LauncherConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Which video inputs the user hid, kept in the launcher config (per device) and keyed by `TvInputInfo` id. */
class InputVisibility(private val store: ConfigStore) {
    val hidden: Flow<Set<String>> = store.config.map { it?.hiddenInputs ?: emptySet() }.distinctUntilChanged()

    suspend fun hide(id: String) = change { it + id }

    suspend fun show(id: String) = change { it - id }

    /** Unhides every input in [sources] that has a real signal. Writes nothing when there is nothing to change. */
    suspend fun revealConnected(sources: List<InputSource>) = reveal { InputRules.revealConnected(it, sources) }

    /** Unhides [inputId] when [state] is the connected `INPUT_STATE_*` value. */
    suspend fun revealOnState(inputId: String, state: Int) = reveal { InputRules.revealOnState(it, inputId, state) }

    private suspend fun reveal(rule: (Set<String>) -> Set<String>) {
        val current = store.config.first()?.hiddenInputs.orEmpty()
        if (current.isNotEmpty() && rule(current) != current) change(rule)
    }

    private suspend fun change(rule: (Set<String>) -> Set<String>) {
        store.update { saved ->
            val config = saved ?: LauncherConfig.Default
            config.copy(hiddenInputs = rule(config.hiddenInputs))
        }
    }
}
