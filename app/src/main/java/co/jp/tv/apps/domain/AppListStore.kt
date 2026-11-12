package co.jp.tv.apps.domain

/** Port: where the last known app list is kept between launches. Blocking; call it off the main thread. */
interface AppListStore {
    fun read(): List<TvApp>
    fun write(apps: List<TvApp>)
}
