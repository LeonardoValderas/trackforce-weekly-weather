package com.leonardo.valderas.meeklyweather.data.remote

import com.leonardo.valderas.meeklyweather.domain.model.DailyForecast
import com.leonardo.valderas.meeklyweather.domain.model.ForecastTarget
import com.leonardo.valderas.meeklyweather.domain.model.WeatherCondition
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import java.io.IOException
import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenMeteoForecastDataSource @Inject constructor(
    private val client: HttpClient,
    private val clock: Clock,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(target: ForecastTarget): WeeklyForecast {
        val response = client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", target.coordinates.latitude)
            parameter("longitude", target.coordinates.longitude)
            parameter("forecast_days", WeeklyForecast.FORECAST_DAYS)
            parameter("timezone", "auto")
            parameter("temperature_unit", "celsius")
            parameter(
                "daily",
                "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
            )
        }
        if (!response.status.isSuccess()) {
            throw IOException("Open-Meteo returned HTTP ${response.status.value}")
        }
        val body = response.bodyAsText()
        return try {
            val forecast = json.decodeFromString<ForecastResponse>(body)
            WeeklyForecast(
                location = target.location,
                timeZone = ZoneId.of(forecast.timezone),
                days = forecast.daily.toDays(),
                fetchedAt = clock.instant(),
            )
        } catch (exception: IllegalArgumentException) {
            throw IOException("Open-Meteo returned an invalid forecast", exception)
        } catch (exception: DateTimeException) {
            throw IOException("Open-Meteo returned an invalid date or time zone", exception)
        }
    }
}

@Serializable
private data class ForecastResponse(val timezone: String, val daily: DailyResponse)

@Serializable
private data class DailyResponse(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    @SerialName("temperature_2m_min") val minimum: List<Double?>,
    @SerialName("temperature_2m_max") val maximum: List<Double?>,
    @SerialName("precipitation_probability_max") val precipitation: List<Int?>? = null,
) {
    fun toDays(): List<DailyForecast> {
        val dayCount = WeeklyForecast.FORECAST_DAYS
        require(time.size == dayCount && weatherCode.size == dayCount)
        require(minimum.size == dayCount && maximum.size == dayCount)
        return time.mapIndexed { index, date ->
            DailyForecast(
                date = LocalDate.parse(date),
                condition = conditionFor(requireNotNull(weatherCode[index])),
                minCelsius = requireNotNull(minimum[index]),
                maxCelsius = requireNotNull(maximum[index]),
                precipitationProbability = precipitation?.getOrNull(index)?.takeIf { it in 0..100 },
            )
        }
    }
}

private fun conditionFor(code: Int): WeatherCondition = when (code) {
    0 -> WeatherCondition.CLEAR
    1 -> WeatherCondition.MAINLY_CLEAR
    2 -> WeatherCondition.PARTLY_CLOUDY
    3 -> WeatherCondition.OVERCAST
    45, 48 -> WeatherCondition.FOG
    51, 53, 55 -> WeatherCondition.DRIZZLE
    56, 57 -> WeatherCondition.FREEZING_DRIZZLE
    61, 63, 65 -> WeatherCondition.RAIN
    66, 67 -> WeatherCondition.FREEZING_RAIN
    71, 73, 75 -> WeatherCondition.SNOW
    77 -> WeatherCondition.SNOW_GRAINS
    80, 81, 82 -> WeatherCondition.RAIN_SHOWERS
    85, 86 -> WeatherCondition.SNOW_SHOWERS
    95 -> WeatherCondition.THUNDERSTORM
    96, 99 -> WeatherCondition.THUNDERSTORM_WITH_HAIL
    else -> WeatherCondition.UNKNOWN
}
