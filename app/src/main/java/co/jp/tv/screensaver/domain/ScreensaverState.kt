package co.jp.tv.screensaver.domain

import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.MediaLibraryState
import co.jp.tv.playlist.domain.PlaylistResolver
import co.jp.tv.playlist.domain.PlaylistSelection
import co.jp.tv.playlist.domain.WallpaperMedia
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherPlace
import co.jp.tv.weather.domain.WeatherRepository
import co.jp.tv.weather.domain.WeatherRules
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Everything the screensaver modes draw from. It is built the same way for the in-app overlay and for the
 * system DreamService, so both look alike and neither needs the home's view model.
 */
data class ScreensaverState(
    val now: LocalDateTime,
    val config: LauncherConfig,
    val weather: Weather?,
    /** Pictures for the "Fotos" slideshow: the protector folder when it has any, else the image playlist. */
    val photos: List<MediaItem>,
    /** Videos for "Live" and for an ambient scene that falls back to a video wallpaper. */
    val videos: PlaylistSelection,
    val wallpaperMedia: WallpaperMedia,
    val mediaLoaded: Boolean,
) {
    companion object {
        fun of(now: LocalDateTime, config: LauncherConfig, weather: Weather?, library: MediaLibraryState): ScreensaverState {
            val wallpaperMedia = WallpaperMedia.of(library, config.videoPlaylist, config.imagePlaylist)
            val photos = library.protector.ifEmpty { PlaylistResolver.resolve(library.images, config.imagePlaylist) }
            return ScreensaverState(now, config, weather, photos, wallpaperMedia.video, wallpaperMedia, library.loaded)
        }
    }
}

object ScreensaverStates {
    /** Milliseconds from [now] to the next full minute (a whole minute when [now] is exactly on one). */
    fun msToNextMinute(now: LocalDateTime): Long {
        val into = now.second * 1000L + now.nano / 1_000_000L
        return 60_000L - into
    }

    /** Emits the wall clock now and then at every minute boundary: the savers show hours and minutes only. */
    fun minuteTicks(now: () -> LocalDateTime = { LocalDateTime.now() }): Flow<LocalDateTime> = flow {
        while (true) {
            val current = now()
            emit(current)
            delay(msToNextMinute(current))
        }
    }

    /** The weather of the chosen place: the cached reading first, then a fresh one at most once a minute (the repository decides). */
    fun weather(config: Flow<LauncherConfig?>, repository: WeatherRepository, ticks: Flow<LocalDateTime>): Flow<Weather?> =
        config.map { (it ?: LauncherConfig.Default).weatherPlace }
            .distinctUntilChanged()
            .flatMapLatest { place -> readings(place, repository, ticks) }

    private fun readings(place: WeatherPlace, repository: WeatherRepository, ticks: Flow<LocalDateTime>): Flow<Weather?> = flow {
        emit(repository.cached(place))
        ticks.distinctUntilChanged { a, b -> a.truncatedTo(ChronoUnit.MINUTES) == b.truncatedTo(ChronoUnit.MINUTES) }
            .collect { now -> emit(repository.current(place, now)) }
    }

    fun flow(
        config: Flow<LauncherConfig?>,
        ticks: Flow<LocalDateTime>,
        library: Flow<MediaLibraryState>,
        weather: Flow<Weather?>,
    ): Flow<ScreensaverState> = combine(config, ticks, library, weather) { saved, now, media, reading ->
        ScreensaverState.of(now, saved ?: LauncherConfig.Default, WeatherRules.withStaleness(reading, now), media)
    }
}
