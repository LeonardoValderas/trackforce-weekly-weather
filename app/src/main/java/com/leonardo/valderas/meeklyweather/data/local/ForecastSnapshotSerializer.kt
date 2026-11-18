package com.leonardo.valderas.meeklyweather.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.leonardo.valderas.meeklyweather.domain.model.DailyForecast
import com.leonardo.valderas.meeklyweather.domain.model.ForecastLocation
import com.leonardo.valderas.meeklyweather.domain.model.WeatherCondition
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import java.io.InputStream
import java.io.OutputStream
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object ForecastSnapshotSerializer : Serializer<WeeklyForecast?> {
    override val defaultValue: WeeklyForecast? = null

    override suspend fun readFrom(input: InputStream): WeeklyForecast? {
        val content = input.readBytes().decodeToString()
        return try {
            Json.decodeFromString<StoredForecast?>(content)?.toForecast()
        } catch (exception: IllegalArgumentException) {
            throw CorruptionException("Invalid forecast snapshot", exception)
        } catch (exception: DateTimeException) {
            throw CorruptionException("Invalid date or time zone in forecast snapshot", exception)
        }
    }

    override suspend fun writeTo(t: WeeklyForecast?, output: OutputStream) {
        output.write(Json.encodeToString(t?.let(StoredForecast::from)).encodeToByteArray())
    }
}

@Serializable
private data class StoredForecast(
    val version: Int,
    val location: ForecastLocation,
    val timeZone: String,
    val fetchedAt: String,
    val days: List<StoredDay>,
) {
    fun toForecast(): WeeklyForecast {
        require(version == VERSION)
        return WeeklyForecast(
            location = location,
            timeZone = ZoneId.of(timeZone),
            days = days.map {
                DailyForecast(
                    date = LocalDate.parse(it.date),
                    condition = it.condition,
                    minCelsius = it.minCelsius,
                    maxCelsius = it.maxCelsius,
                    precipitationProbability = it.precipitationProbability,
                )
            },
            fetchedAt = Instant.parse(fetchedAt),
        )
    }

    companion object {
        private const val VERSION = 1

        fun from(forecast: WeeklyForecast): StoredForecast = StoredForecast(
            version = VERSION,
            location = forecast.location,
            timeZone = forecast.timeZone.id,
            fetchedAt = forecast.fetchedAt.toString(),
            days = forecast.days.map {
                StoredDay(
                    date = it.date.toString(),
                    condition = it.condition,
                    minCelsius = it.minCelsius,
                    maxCelsius = it.maxCelsius,
                    precipitationProbability = it.precipitationProbability,
                )
            },
        )
    }
}

@Serializable
private data class StoredDay(
    val date: String,
    val condition: WeatherCondition,
    val minCelsius: Double,
    val maxCelsius: Double,
    val precipitationProbability: Int?,
)
