package com.leonardo.valderas.meeklyweather.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leonardo.valderas.meeklyweather.domain.usecase.ObserveWeeklyForecastUseCase
import com.leonardo.valderas.meeklyweather.domain.usecase.RefreshWeeklyForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeeklyWeatherViewModel @Inject constructor(
    private val observeForecast: ObserveWeeklyForecastUseCase,
    private val refreshForecast: RefreshWeeklyForecastUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(WeeklyWeatherUiState())
    val uiState = mutableUiState.asStateFlow()

    private var screenOpened = false
    private var cacheObservation: Job? = null

    init {
        observeCacheIfNeeded()
    }

    fun onScreenOpened(hasLocationPermission: Boolean, awaitingPermission: Boolean) {
        if (screenOpened) return
        screenOpened = true
        if (!awaitingPermission) refresh(hasLocationPermission)
    }

    fun refresh(hasLocationPermission: Boolean) {
        if (uiState.value.isRefreshing) return
        mutableUiState.update { it.copy(isStarting = false, isRefreshing = true, refreshFailed = false) }
        observeCacheIfNeeded()
        viewModelScope.launch {
            try {
                refreshForecast(hasLocationPermission)
                observeCacheIfNeeded()
            } catch (_: IOException) {
                mutableUiState.update { it.copy(refreshFailed = true) }
            } finally {
                mutableUiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun observeCacheIfNeeded() {
        if (cacheObservation?.isActive == true) return
        mutableUiState.update { it.copy(isReadingCache = true) }
        cacheObservation = viewModelScope.launch {
            observeForecast()
                .catch { error ->
                    if (error !is IOException) throw error
                    mutableUiState.update { it.copy(isReadingCache = false, cacheReadFailed = true) }
                }
                .collect { forecast ->
                    mutableUiState.update {
                        it.copy(forecast = forecast, isReadingCache = false, cacheReadFailed = false)
                    }
                }
        }
    }
}
