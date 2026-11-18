package com.leonardo.valderas.meeklyweather.ui.presentation

import androidx.lifecycle.viewModelScope
import com.leonardo.valderas.meeklyweather.domain.model.DailyForecast
import com.leonardo.valderas.meeklyweather.domain.utils.DeviceLocation
import com.leonardo.valderas.meeklyweather.domain.model.ForecastLocation
import com.leonardo.valderas.meeklyweather.domain.repository.ForecastRepository
import com.leonardo.valderas.meeklyweather.domain.model.ForecastTarget
import com.leonardo.valderas.meeklyweather.domain.usecase.ObserveWeeklyForecastUseCase
import com.leonardo.valderas.meeklyweather.domain.usecase.RefreshWeeklyForecastUseCase
import com.leonardo.valderas.meeklyweather.domain.model.WeatherCondition
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyWeatherViewModelTest {
    @Test
    fun failedRefreshKeepsCachedForecastAndTimestampVisible() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val cached = forecast()
        val finishRefresh = CompletableDeferred<Unit>()
        val repository = FakeRepository(cached) {
            finishRefresh.await()
            throw IOException("Offline")
        }
        val viewModel = createViewModel(repository)
        try {
            viewModel.onScreenOpened(hasLocationPermission = false, awaitingPermission = false)
            runCurrent()

            assertTrue(viewModel.uiState.value.isRefreshing)
            assertEquals(cached, viewModel.uiState.value.forecast)

            viewModel.refresh(hasLocationPermission = false)
            finishRefresh.complete(Unit)
            runCurrent()

            assertEquals(1, repository.targets.size)
            assertEquals(cached, viewModel.uiState.value.forecast)
            assertTrue(viewModel.uiState.value.hasError)
            assertFalse(viewModel.uiState.value.isBusy)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun emptyCacheWaitsForPermissionThenAllowsRetryAfterOfflineFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val updated = forecast()
        var online = false
        val repository = FakeRepository(null) {
            if (online) updated else throw IOException("Offline")
        }
        val viewModel = createViewModel(repository)
        try {
            viewModel.onScreenOpened(hasLocationPermission = false, awaitingPermission = true)
            runCurrent()

            assertTrue(viewModel.uiState.value.isBusy)
            assertTrue(repository.targets.isEmpty())

            viewModel.refresh(hasLocationPermission = false)
            runCurrent()

            assertEquals(ForecastLocation.DEFAULT_MONTREAL, repository.targets.single().location)
            assertNull(viewModel.uiState.value.forecast)
            assertTrue(viewModel.uiState.value.hasError)
            assertFalse(viewModel.uiState.value.isBusy)

            online = true
            viewModel.refresh(hasLocationPermission = false)
            runCurrent()

            assertEquals(updated, viewModel.uiState.value.forecast)
            assertFalse(viewModel.uiState.value.hasError)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private fun createViewModel(repository: ForecastRepository): WeeklyWeatherViewModel {
        val deviceLocation = object : DeviceLocation {
            override suspend fun currentFix() = error("Permission was denied")
        }
        return WeeklyWeatherViewModel(
            ObserveWeeklyForecastUseCase(repository),
            RefreshWeeklyForecastUseCase(deviceLocation, repository),
        )
    }

    private fun forecast(): WeeklyForecast {
        val firstDay = LocalDate.parse("2026-11-17")
        return WeeklyForecast(
            location = ForecastLocation.DEFAULT_MONTREAL,
            timeZone = ZoneId.of("America/Toronto"),
            days = List(WeeklyForecast.FORECAST_DAYS) { index ->
                DailyForecast(firstDay.plusDays(index.toLong()), WeatherCondition.CLEAR, 3.0, 8.0, null)
            },
            fetchedAt = Instant.parse("2026-11-17T22:00:00Z"),
        )
    }

    private class FakeRepository(
        cached: WeeklyForecast?,
        private val fetch: suspend () -> WeeklyForecast,
    ) : ForecastRepository {
        override val forecast = MutableStateFlow(cached)
        val targets = mutableListOf<ForecastTarget>()

        override suspend fun refresh(target: ForecastTarget) {
            targets.add(target)
            forecast.value = fetch()
        }
    }
}
