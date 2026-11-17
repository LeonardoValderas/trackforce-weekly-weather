package com.leonardo.valderas.meeklyweather.data.repository

import com.leonardo.valderas.meeklyweather.data.local.ForecastLocalDataSource
import com.leonardo.valderas.meeklyweather.data.remote.OpenMeteoForecastDataSource
import com.leonardo.valderas.meeklyweather.domain.repository.ForecastRepository
import com.leonardo.valderas.meeklyweather.domain.model.ForecastTarget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastRepositoryImpl @Inject constructor(
    private val remote: OpenMeteoForecastDataSource,
    private val local: ForecastLocalDataSource,
) : ForecastRepository {
    override val forecast = local.forecast

    override suspend fun refresh(target: ForecastTarget) {
        local.replace(remote.fetch(target))
    }
}
