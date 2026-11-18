package com.leonardo.valderas.meeklyweather.domain.utils

import com.leonardo.valderas.meeklyweather.domain.model.Coordinates

interface DeviceLocation {
    suspend fun currentFix(): Coordinates?
}
