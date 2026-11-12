package co.jp.tv.apps.domain

import kotlinx.coroutines.flow.Flow

/** Port: source of installed apps and the way to launch them. */
interface AppCatalog {
    /** Emits the current list and again whenever packages are added, removed or changed. */
    val apps: Flow<List<TvApp>>

    /** Starts [app]; [origin], when known, is where the window animation should grow from. */
    fun launch(app: TvApp, origin: LaunchOrigin? = null)
}
