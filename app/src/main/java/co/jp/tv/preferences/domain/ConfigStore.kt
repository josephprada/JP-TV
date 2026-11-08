package co.jp.tv.preferences.domain

import kotlinx.coroutines.flow.Flow

/** Port: persistence of [LauncherConfig]. */
interface ConfigStore {
    /** Current configuration; null until something has been saved (the first run). */
    val config: Flow<LauncherConfig?>

    /** Atomically rewrites the saved configuration; [transform] receives null on the first run. */
    suspend fun update(transform: (LauncherConfig?) -> LauncherConfig)
}
