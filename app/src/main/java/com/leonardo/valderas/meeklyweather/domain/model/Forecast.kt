package com.leonardo.valderas.meeklyweather.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WeeklyForecast(
    val location: ForecastLocation,
    val timeZone: ZoneId,
    val days: List<DailyForecast>,
    val fetchedAt: Instant,
) {
    init {
        require(days.size == FORECAST_DAYS)
        require(days.zipWithNext().all { (previous, next) ->
            previous.date.plusDays(1) == next.date
        })
    }

    companion object {
        const val FORECAST_DAYS = 7
    }
}

data class DailyForecast(
    val date: LocalDate,
    val condition: WeatherCondition,
    val minCelsius: Double,
    val maxCelsius: Double,
    val precipitationProbability: Int?,
) {
    init {
        require(minCelsius.isFinite() && maxCelsius.isFinite() && minCelsius <= maxCelsius)
        require(precipitationProbability == null || precipitationProbability in 0..100)
    }
}

enum class WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    FREEZING_DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    SNOW,
    SNOW_GRAINS,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    THUNDERSTORM_WITH_HAIL,
    UNKNOWN,
}
