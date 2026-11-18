package com.leonardo.valderas.meeklyweather.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leonardo.valderas.meeklyweather.R
import com.leonardo.valderas.meeklyweather.domain.model.DailyForecast
import com.leonardo.valderas.meeklyweather.domain.model.WeatherCondition
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastDayCard(day: DailyForecast, locale: Locale) {
    val date = remember(day.date, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale).format(day.date)
    }
    val integerFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val percentFormat = remember(locale) { NumberFormat.getPercentInstance(locale) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(date, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(day.condition.label), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(
                    R.string.forecast_temperature_range,
                    integerFormat.format(day.minCelsius.roundToInt()),
                    integerFormat.format(day.maxCelsius.roundToInt()),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            day.precipitationProbability?.let { probability ->
                Text(
                    text = stringResource(R.string.forecast_precipitation, percentFormat.format(probability / 100.0)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val WeatherCondition.label: Int
    get() = when (this) {
        WeatherCondition.CLEAR -> R.string.weather_clear
        WeatherCondition.MAINLY_CLEAR -> R.string.weather_mainly_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
        WeatherCondition.OVERCAST -> R.string.weather_overcast
        WeatherCondition.FOG -> R.string.weather_fog
        WeatherCondition.DRIZZLE -> R.string.weather_drizzle
        WeatherCondition.FREEZING_DRIZZLE -> R.string.weather_freezing_drizzle
        WeatherCondition.RAIN -> R.string.weather_rain
        WeatherCondition.FREEZING_RAIN -> R.string.weather_freezing_rain
        WeatherCondition.SNOW -> R.string.weather_snow
        WeatherCondition.SNOW_GRAINS -> R.string.weather_snow_grains
        WeatherCondition.RAIN_SHOWERS -> R.string.weather_rain_showers
        WeatherCondition.SNOW_SHOWERS -> R.string.weather_snow_showers
        WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
        WeatherCondition.THUNDERSTORM_WITH_HAIL -> R.string.weather_thunderstorm_with_hail
        WeatherCondition.UNKNOWN -> R.string.weather_unknown
    }
