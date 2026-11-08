package co.jp.tv.apps.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import co.jp.tv.apps.domain.TvApp

/** Lazy icon source for the UI; themes read it from [LocalAppIcons] and never touch PackageManager. */
fun interface AppIcons {
    suspend fun load(app: TvApp): ImageBitmap?
}

val LocalAppIcons = staticCompositionLocalOf<AppIcons> { AppIcons { null } }
