package com.leonardo.valderas.meeklyweather.domain.usecase

import com.leonardo.valderas.meeklyweather.domain.repository.ForecastRepository
import javax.inject.Inject

class ObserveWeeklyForecastUseCase @Inject constructor(private val repository: ForecastRepository) {
    operator fun invoke() = repository.forecast
}
