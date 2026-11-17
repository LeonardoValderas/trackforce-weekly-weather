package com.leonardo.valderas.meeklyweather.data.repository

import androidx.datastore.core.DataStoreFactory
import com.leonardo.valderas.meeklyweather.data.local.ForecastLocalDataSource
import com.leonardo.valderas.meeklyweather.data.local.ForecastSnapshotSerializer
import com.leonardo.valderas.meeklyweather.data.remote.OpenMeteoForecastDataSource
import com.leonardo.valderas.meeklyweather.domain.model.Coordinates
import com.leonardo.valderas.meeklyweather.domain.model.ForecastLocation
import com.leonardo.valderas.meeklyweather.domain.model.ForecastTarget
import com.leonardo.valderas.meeklyweather.domain.model.WeatherCondition
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ForecastRepositoryImplTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val snapshotFile
        get() = temporaryFolder.root.resolve("weekly_forecast.json")
    private val fetchedAt = Instant.parse("2026-11-17T22:00:00Z")
    private val clock = Clock.fixed(fetchedAt, ZoneOffset.UTC)
    private val target = ForecastTarget(
        coordinates = Coordinates(latitude = -23.55, longitude = -46.63),
        location = ForecastLocation.DEVICE,
    )

    @Test
    fun refreshSavesSevenDayForecastThatCanBeReopenedOffline() = runTest {
        val saved = mockClient { request ->
            val url = request.url
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(URLProtocol.HTTPS, url.protocol)
            assertEquals("api.open-meteo.com", url.host)
            assertEquals("/v1/forecast", url.encodedPath)
            assertEquals("-23.55", url.parameters["latitude"])
            assertEquals("-46.63", url.parameters["longitude"])
            assertEquals("7", url.parameters["forecast_days"])
            assertEquals("auto", url.parameters["timezone"])
            assertEquals("celsius", url.parameters["temperature_unit"])
            assertEquals(
                setOf(
                    "weather_code",
                    "temperature_2m_min",
                    "temperature_2m_max",
                    "precipitation_probability_max",
                ),
                url.parameters["daily"]?.split(",")?.toSet(),
            )
            respond(VALID_FORECAST)
        }.use { client ->
            withRepository(client) { repository ->
                assertNull(repository.forecast.first())
                repository.refresh(target)
                requireNotNull(repository.forecast.first())
            }
        }

        assertEquals(ForecastLocation.DEVICE, saved.location)
        assertEquals(ZoneId.of("America/Sao_Paulo"), saved.timeZone)
        assertEquals(fetchedAt, saved.fetchedAt)
        assertEquals(
            (17..23).map { LocalDate.of(2026, 11, it) },
            saved.days.map { it.date },
        )
        assertEquals(WeatherCondition.CLEAR, saved.days.first().condition)
        assertEquals(19.5, saved.days.first().minCelsius, 0.0)
        assertEquals(26.0, saved.days.first().maxCelsius, 0.0)
        assertEquals(10, saved.days.first().precipitationProbability)
        assertNull(saved.days[1].precipitationProbability)
        assertEquals(WeatherCondition.UNKNOWN, saved.days[2].condition)

        mockClient { error("Reading the persisted forecast must not make a request") }.use { client ->
            withRepository(client) { repository ->
                assertEquals(saved, repository.forecast.first())
            }
        }
    }

    @Test
    fun malformedResponseLeavesPersistedForecastUntouched() = runTest {
        var body = VALID_FORECAST
        mockClient { respond(body) }.use { client ->
            withRepository(client) { repository ->
                repository.refresh(target)
                val saved = requireNotNull(repository.forecast.first())
                val snapshot = snapshotFile.readBytes()
                body = VALID_FORECAST.replace("2026-11-18", "2026-11-17")

                val failure = runCatching { repository.refresh(target) }.exceptionOrNull()

                assertTrue("Expected a validation failure, got $failure", failure is IOException)
                assertEquals(saved, repository.forecast.first())
                assertArrayEquals(snapshot, snapshotFile.readBytes())
            }
        }
    }

    @Test
    fun networkFailureLeavesPersistedForecastUntouched() = runTest {
        var offline = false
        mockClient {
            if (offline) throw IOException("No network connection")
            respond(VALID_FORECAST)
        }.use { client ->
            withRepository(client) { repository ->
                repository.refresh(target)
                val saved = requireNotNull(repository.forecast.first())
                val snapshot = snapshotFile.readBytes()
                offline = true

                val failure = runCatching { repository.refresh(target) }.exceptionOrNull()

                assertTrue("Expected a network failure, got $failure", failure is IOException)
                assertEquals(saved, repository.forecast.first())
                assertArrayEquals(snapshot, snapshotFile.readBytes())
            }
        }
    }

    private fun mockClient(handler: MockRequestHandler): HttpClient = HttpClient(MockEngine) {
        engine { addHandler(handler) }
    }

    private suspend fun <T> withRepository(
        client: HttpClient,
        block: suspend (ForecastRepositoryImpl) -> T,
    ): T {
        val storeJob = SupervisorJob()
        val store = DataStoreFactory.create(
            serializer = ForecastSnapshotSerializer,
            scope = CoroutineScope(Dispatchers.IO + storeJob),
            produceFile = { snapshotFile },
        )
        try {
            val remote = OpenMeteoForecastDataSource(client, clock)
            return block(ForecastRepositoryImpl(remote, ForecastLocalDataSource(store)))
        } finally {
            storeJob.cancelAndJoin()
        }
    }

    private companion object {
        const val VALID_FORECAST = """
            {
              "timezone": "America/Sao_Paulo",
              "daily": {
                "time": ["2026-11-17", "2026-11-18", "2026-11-19", "2026-11-20", "2026-11-21", "2026-11-22", "2026-11-23"],
                "weather_code": [0, 2, 999, 45, 61, 3, 95],
                "temperature_2m_min": [19.5, 19, 21, 18, 20, 23, 22],
                "temperature_2m_max": [26, 27, 28, 25, 27, 29, 30],
                "precipitation_probability_max": [10, null, 40, 50, 60, 70, 80]
              }
            }
        """
    }
}
