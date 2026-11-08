package co.jp.tv.apps.domain

import kotlinx.coroutines.flow.Flow

/** Port: source of installed apps and the way to launch them. */
interface AppCatalog {
    /** Emits the current list and again whenever packages are added, removed or changed. */
    val apps: Flow<List<TvApp>>

    fun launch(app: TvApp)
}
