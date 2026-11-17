package com.leonardo.valderas.meeklyweather.data.local

import androidx.datastore.core.DataStore
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import javax.inject.Inject

class ForecastLocalDataSource @Inject constructor(private val store: DataStore<WeeklyForecast?>) {
    val forecast = store.data

    suspend fun replace(forecast: WeeklyForecast) {
        store.updateData { forecast }
    }
}
