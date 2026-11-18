package com.leonardo.valderas.meeklyweather.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.leonardo.valderas.meeklyweather.data.repository.ForecastRepositoryImpl
import com.leonardo.valderas.meeklyweather.data.local.ForecastSnapshotSerializer
import com.leonardo.valderas.meeklyweather.data.location.AndroidLocationAdapter
import com.leonardo.valderas.meeklyweather.domain.utils.DeviceLocation
import com.leonardo.valderas.meeklyweather.domain.repository.ForecastRepository
import com.leonardo.valderas.meeklyweather.domain.model.WeeklyForecast
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import java.time.Clock
import javax.inject.Singleton

private val Context.forecastDataStore by dataStore(
    fileName = "weekly_forecast.json",
    serializer = ForecastSnapshotSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { null },
)

@Module
@InstallIn(SingletonComponent::class)
abstract class ForecastModule {
    @Binds
    abstract fun bindRepository(implementation: ForecastRepositoryImpl): ForecastRepository

    @Binds
    abstract fun bindLocation(implementation: AndroidLocationAdapter): DeviceLocation

    companion object {
        @Provides
        @Singleton
        fun provideForecastStore(@ApplicationContext context: Context): DataStore<WeeklyForecast?> =
            context.forecastDataStore

        @Provides
        @Singleton
        fun provideHttpClient(): HttpClient = HttpClient(Android) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
            }
        }

        @Provides
        fun provideClock(): Clock = Clock.systemUTC()
    }
}
