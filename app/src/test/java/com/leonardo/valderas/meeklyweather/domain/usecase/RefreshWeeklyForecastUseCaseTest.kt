package com.leonardo.valderas.meeklyweather.domain.usecase

import com.leonardo.valderas.meeklyweather.domain.model.Coordinates
import com.leonardo.valderas.meeklyweather.domain.model.ForecastLocation
import com.leonardo.valderas.meeklyweather.domain.model.ForecastTarget
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import com.leonardo.valderas.meeklyweather.domain.repository.ForecastRepository
import com.leonardo.valderas.meeklyweather.domain.utils.DeviceLocation
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshWeeklyForecastUseCaseTest {
    @Test
    fun grantedPermissionUsesDeviceCoordinates() = runTest {
        val coordinates = Coordinates(latitude = -23.55, longitude = -46.63)
        val deviceLocation = FakeDeviceLocation(coordinates)
        val repository = RecordingRepository()

        RefreshWeeklyForecastUseCase(deviceLocation, repository)(hasLocationPermission = true)

        assertEquals(1, deviceLocation.lookups)
        assertEquals(ForecastTarget(coordinates, ForecastLocation.DEVICE), repository.targets.single())
    }

    @Test
    fun deniedPermissionSkipsLocationLookupAndUsesMontreal() = runTest {
        val deviceLocation = FakeDeviceLocation(Coordinates(latitude = -23.55, longitude = -46.63))
        val repository = RecordingRepository()

        RefreshWeeklyForecastUseCase(deviceLocation, repository)(hasLocationPermission = false)

        assertEquals(0, deviceLocation.lookups)
        assertEquals(montreal, repository.targets.single())
    }

    @Test
    fun unavailableDeviceFixUsesMontrealEvenWithPermission() = runTest {
        val deviceLocation = FakeDeviceLocation(null)
        val repository = RecordingRepository()

        RefreshWeeklyForecastUseCase(deviceLocation, repository)(hasLocationPermission = true)

        assertEquals(1, deviceLocation.lookups)
        assertEquals(montreal, repository.targets.single())
    }

    private class FakeDeviceLocation(private val fix: Coordinates?) : DeviceLocation {
        var lookups = 0
            private set

        override suspend fun currentFix(): Coordinates? {
            lookups++
            return fix
        }
    }

    private class RecordingRepository : ForecastRepository {
        override val forecast = emptyFlow<WeeklyForecast?>()
        val targets = mutableListOf<ForecastTarget>()

        override suspend fun refresh(target: ForecastTarget) {
            targets.add(target)
        }
    }

    private companion object {
        val montreal = ForecastTarget(
            coordinates = Coordinates(latitude = 45.50, longitude = -73.57),
            location = ForecastLocation.DEFAULT_MONTREAL,
        )
    }
}
