package co.jp.tv.preferences.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.weather.domain.WeatherPlace
import kotlinx.coroutines.delay

/**
 * Town picker for the weather: the quick-pick towns as chips, plus a search box for any other town in
 * Colombia. Everything is reachable with the D-pad; the search box raises the on-screen keyboard like the
 * launcher name field and shows its matches as more chips once two letters are typed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WeatherPlaceCard(
    selected: WeatherPlace,
    onSelect: (WeatherPlace) -> Unit,
    search: suspend (String) -> List<WeatherPlace>,
    accent: Color,
    initialQuery: String = "",
) {
    var query by remember { mutableStateOf(initialQuery) }
    // null: nothing searched yet (or the query is too short); empty: searched, no matches.
    var results by remember { mutableStateOf<List<WeatherPlace>?>(null) }
    var searching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val text = query.trim()
        if (text.length < MIN_QUERY) {
            results = null
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(DEBOUNCE_MS)
        results = search(text)
        searching = false
    }

    // A town chosen from a search that is not a quick pick stays visible as the first chip.
    val picks = if (selected in WeatherPlace.QuickPicks) WeatherPlace.QuickPicks else listOf(selected) + WeatherPlace.QuickPicks
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        picks.forEach { place ->
            Chip(place.name, selected = place == selected, accent = accent, onClick = { onSelect(place) })
        }
    }
    TextEntryField(
        value = query,
        onValueChange = { query = it.take(MAX_QUERY) },
        accent = accent,
        label = stringResource(R.string.customize_weather_search_hint),
        textStyle = TextStyle(color = JpColors.Paper, fontSize = 11.sp, fontFamily = JpFonts.Body),
        placeholder = stringResource(R.string.customize_weather_search),
        placeholderStyle = TextStyle(color = JpColors.Muted, fontSize = 11.sp, fontFamily = JpFonts.Body),
    )
    val found = results
    when {
        searching -> Note(stringResource(R.string.customize_weather_searching))
        found != null && found.isEmpty() -> Note(stringResource(R.string.customize_weather_no_results))
        found != null -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            found.forEach { place ->
                Chip(place.label, selected = place == selected, accent = accent, onClick = { onSelect(place) })
            }
        }
    }
}

@Composable
private fun Note(text: String) {
    BasicText(text, style = TextStyle(color = JpColors.Muted, fontSize = 9.sp, fontFamily = JpFonts.Body))
}

private const val MIN_QUERY = 2
private const val MAX_QUERY = 40
private const val DEBOUNCE_MS = 450L
