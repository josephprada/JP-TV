package co.jp.tv.apps.domain

/** Screen rectangle, in pixels, of the tile an app is launched from; the launch animation grows out of it. */
data class LaunchOrigin(val x: Int, val y: Int, val width: Int, val height: Int)
