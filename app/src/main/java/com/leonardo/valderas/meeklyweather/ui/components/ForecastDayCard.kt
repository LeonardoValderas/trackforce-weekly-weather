package com.leonardo.valderas.meeklyweather.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val colors = MaterialTheme.colorScheme

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, colors.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colors.surfaceContainerLow,
            contentColor = colors.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(date, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(day.condition.label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.primary,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = stringResource(
                            R.string.forecast_temperature_range,
                            integerFormat.format(day.minCelsius.roundToInt()),
                            integerFormat.format(day.maxCelsius.roundToInt()),
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                day.precipitationProbability?.let { probability ->
                    Text(
                        text = stringResource(
                            R.string.forecast_precipitation,
                            percentFormat.format(probability / 100.0),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
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
