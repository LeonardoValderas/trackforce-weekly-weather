package com.leonardo.valderas.meeklyweather.domain.model

data class Coordinates(val latitude: Double, val longitude: Double) {
    init {
        require(latitude in -90.0..90.0)
        require(longitude in -180.0..180.0)
    }
}

enum class ForecastLocation {
    DEVICE,
    DEFAULT_MONTREAL,
}

data class ForecastTarget(val coordinates: Coordinates, val location: ForecastLocation)

object ForecastTargetFactory {
    private val defaultTarget = ForecastTarget(
        coordinates = Coordinates(latitude = 45.50, longitude = -73.57),
        location = ForecastLocation.DEFAULT_MONTREAL,
    )

    fun create(deviceFix: Coordinates?): ForecastTarget {
        return deviceFix?.let { ForecastTarget(it, ForecastLocation.DEVICE) } ?: defaultTarget
    }
}
