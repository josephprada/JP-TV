package co.jp.tv.weather.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.jp.tv.R
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherCondition

/** Spanish name of a condition, for descriptions and the picker. */
@Composable
fun weatherLabel(condition: WeatherCondition, isDay: Boolean = true): String = stringResource(
    when (condition) {
        WeatherCondition.Clear -> if (isDay) R.string.weather_clear else R.string.weather_clear_night
        WeatherCondition.PartlyCloudy -> R.string.weather_partly_cloudy
        WeatherCondition.Cloudy -> R.string.weather_cloudy
        WeatherCondition.Fog -> R.string.weather_fog
        WeatherCondition.Drizzle -> R.string.weather_drizzle
        WeatherCondition.Rain -> R.string.weather_rain
        WeatherCondition.Storm -> R.string.weather_storm
    },
)

/**
 * Icon plus temperature ("24°") in the host theme's colour and type. Draws nothing without a reading, so a
 * theme never shows a blank slot or a placeholder; an out-of-date reading stays but dims.
 */
@Composable
fun WeatherBadge(
    weather: Weather?,
    color: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    gap: Dp = 5.dp,
) {
    if (weather == null) return
    val description = stringResource(
        if (weather.stale) R.string.cd_weather_stale else R.string.cd_weather,
        weatherLabel(weather.condition, weather.isDay),
        weather.temperatureC,
        weather.placeName,
    )
    Row(
        modifier
            .alpha(if (weather.stale) STALE_ALPHA else 1f)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        WeatherIcon(weather.condition, weather.isDay, color, size = iconSize)
        BasicText(text = "${weather.temperatureC}°", style = textStyle.copy(color = color))
    }
}

private const val STALE_ALPHA = 0.5f
