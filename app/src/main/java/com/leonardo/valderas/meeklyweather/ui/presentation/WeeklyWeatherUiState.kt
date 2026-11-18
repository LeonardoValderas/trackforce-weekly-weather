package com.leonardo.valderas.meeklyweather.ui.presentation

import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast

data class WeeklyWeatherUiState(
    val forecast: WeeklyForecast? = null,
    val isReadingCache: Boolean = true,
    val isStarting: Boolean = true,
    val isRefreshing: Boolean = false,
    val cacheReadFailed: Boolean = false,
    val refreshFailed: Boolean = false,
) {
    val isBusy: Boolean
        get() = isReadingCache || isStarting || isRefreshing

    val hasError: Boolean
        get() = cacheReadFailed || refreshFailed
}
