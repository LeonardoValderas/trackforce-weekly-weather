package com.leonardo.valderas.meeklyweather.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.leonardo.valderas.meeklyweather.R
import com.leonardo.valderas.meeklyweather.ui.components.ForecastDayCard
import com.leonardo.valderas.meeklyweather.domain.model.ForecastLocation
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import com.leonardo.valderas.meeklyweather.ui.presentation.WeeklyWeatherUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun WeeklyWeatherScreen(state: WeeklyWeatherUiState, onRefresh: () -> Unit) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val forecast = state.forecast
            if (forecast != null) {
                ForecastContent(
                    forecast = forecast,
                    busy = state.isBusy,
                    refreshing = state.isRefreshing,
                    showError = state.hasError && !state.isBusy,
                    onRefresh = onRefresh,
                )
            } else {
                ForecastPlaceholder(loading = state.isBusy, onRetry = onRefresh)
            }
        }
    }
}

@Composable
private fun ForecastContent(
    forecast: WeeklyForecast,
    busy: Boolean,
    refreshing: Boolean,
    showError: Boolean,
    onRefresh: () -> Unit,
) {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    val deviceTimeZone = ZoneId.systemDefault()
    val lastUpdated = remember(forecast.fetchedAt, locale, deviceTimeZone) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(deviceTimeZone)
            .format(forecast.fetchedAt)
    }
    val locationLabel = when (forecast.location) {
        ForecastLocation.DEVICE -> R.string.forecast_device_location
        ForecastLocation.DEFAULT_MONTREAL -> R.string.forecast_default_location
    }

    LazyColumn(
        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.forecast_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(locationLabel), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(R.string.forecast_last_updated, lastUpdated),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (refreshing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (!showError) {
                        OutlinedButton(onClick = onRefresh, enabled = !busy) {
                            Text(stringResource(R.string.forecast_refresh))
                        }
                    }
                }
            }
        }
        if (showError) {
            item { RefreshError(onRetry = onRefresh) }
        }
        items(forecast.days, key = { it.date.toEpochDay() }) { day ->
            ForecastDayCard(day = day, locale = locale)
        }
    }
}

@Composable
private fun RefreshError(onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)) {
            Text(
                text = stringResource(R.string.forecast_refresh_failed),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.forecast_retry))
            }
        }
    }
}

@Composable
private fun ForecastPlaceholder(loading: Boolean, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading) {
                CircularProgressIndicator()
                Text(stringResource(R.string.forecast_loading))
            } else {
                Text(
                    text = stringResource(R.string.forecast_unavailable),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Text(
                    text = stringResource(R.string.forecast_unavailable_detail),
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.forecast_retry))
                }
            }
        }
    }
}
