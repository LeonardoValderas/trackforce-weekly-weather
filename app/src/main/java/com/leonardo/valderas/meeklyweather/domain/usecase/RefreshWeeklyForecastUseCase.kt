package com.leonardo.valderas.meeklyweather.domain.usecase

import com.leonardo.valderas.meeklyweather.domain.utils.DeviceLocation
import com.leonardo.valderas.meeklyweather.domain.model.ForecastTargetFactory
import com.leonardo.valderas.meeklyweather.domain.repository.ForecastRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex

@Singleton
class RefreshWeeklyForecastUseCase @Inject constructor(
    private val deviceLocation: DeviceLocation,
    private val repository: ForecastRepository,
) {
    private val refreshMutex = Mutex()

    suspend operator fun invoke(hasLocationPermission: Boolean) {
        if (!refreshMutex.tryLock()) return
        try {
            val deviceFix = if (hasLocationPermission) deviceLocation.currentFix() else null
            repository.refresh(ForecastTargetFactory.create(deviceFix))
        } finally {
            refreshMutex.unlock()
        }
    }
}
