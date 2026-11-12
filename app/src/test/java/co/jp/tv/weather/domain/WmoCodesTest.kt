package co.jp.tv.weather.domain

import co.jp.tv.weather.domain.WeatherCondition.Clear
import co.jp.tv.weather.domain.WeatherCondition.Cloudy
import co.jp.tv.weather.domain.WeatherCondition.Drizzle
import co.jp.tv.weather.domain.WeatherCondition.Fog
import co.jp.tv.weather.domain.WeatherCondition.PartlyCloudy
import co.jp.tv.weather.domain.WeatherCondition.Rain
import co.jp.tv.weather.domain.WeatherCondition.Storm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class WmoCodesTest {
    @ParameterizedTest(name = "WMO {0} is {1}")
    @CsvSource(
        "0,Clear", "1,Clear",
        "2,PartlyCloudy",
        "3,Cloudy",
        "45,Fog", "48,Fog",
        "51,Drizzle", "53,Drizzle", "55,Drizzle", "56,Drizzle", "57,Drizzle",
        "61,Rain", "63,Rain", "65,Rain", "66,Rain", "67,Rain",
        "80,Rain", "81,Rain", "82,Rain",
        "95,Storm", "96,Storm", "99,Storm",
    )
    fun `every documented rain, fog and storm code maps to its condition`(code: Int, expected: String) {
        assertEquals(WeatherCondition.valueOf(expected), WmoCodes.condition(code))
    }

    @ParameterizedTest(name = "snow code {0} reads as cloudy")
    @CsvSource("71", "73", "75", "77", "85", "86")
    fun `snow has no icon of its own and reads as cloudy`(code: Int) {
        assertEquals(Cloudy, WmoCodes.condition(code))
    }

    @ParameterizedTest(name = "unknown code {0} reads as cloudy")
    @CsvSource("-1", "4", "44", "100", "999")
    fun `an unknown code falls back to cloudy`(code: Int) {
        assertEquals(Cloudy, WmoCodes.condition(code))
    }

    @org.junit.jupiter.api.Test
    fun `the seven conditions are all reachable`() {
        val reached = listOf(0, 2, 3, 45, 51, 61, 95).map { WmoCodes.condition(it) }.toSet()
        assertEquals(setOf(Clear, PartlyCloudy, Cloudy, Fog, Drizzle, Rain, Storm), reached)
    }
}
