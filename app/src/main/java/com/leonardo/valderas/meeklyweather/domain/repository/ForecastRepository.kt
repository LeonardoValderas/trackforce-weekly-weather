package com.leonardo.valderas.meeklyweather.domain.repository

import com.leonardo.valderas.meeklyweather.domain.model.ForecastTarget
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import kotlinx.coroutines.flow.Flow

interface ForecastRepository {
    val forecast: Flow<WeeklyForecast?>

    suspend fun refresh(target: ForecastTarget)
}
