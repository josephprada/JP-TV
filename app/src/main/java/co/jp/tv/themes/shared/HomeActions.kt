package co.jp.tv.themes.shared

import co.jp.tv.apps.domain.TvApp

interface HomeActions {
    fun launch(app: TvApp)
    fun focus(index: Int)
    fun openNetwork()
    fun openSettings()
    fun openThemes()

    /** Opens the app editor, focused on [focus] when given. */
    fun openEditApps(focus: TvApp?)
}
