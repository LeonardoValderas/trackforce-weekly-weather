package com.leonardo.valderas.meeklyweather.data.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.leonardo.valderas.meeklyweather.domain.model.Coordinates
import com.leonardo.valderas.meeklyweather.domain.utils.DeviceLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class AndroidLocationAdapter @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DeviceLocation {
    override suspend fun currentFix(): Coordinates? {
        if (ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return null
        }
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        if (!LocationManagerCompat.isLocationEnabled(manager)) return null

        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }.filter(manager::isProviderEnabled)

        return withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            channelFlow {
                for (provider in providers) {
                    launch {
                        val fix = try {
                            requestFix(manager, provider)
                        } catch (_: SecurityException) {
                            // A provider may reject approximate access on older Android versions.
                            null
                        } catch (_: IllegalArgumentException) {
                            // The provider can disappear after the enabled check.
                            null
                        }
                        if (fix != null) send(fix)
                    }
                }
            }.firstOrNull()
        }
    }

    @RequiresPermission(ACCESS_COARSE_LOCATION)
    private suspend fun requestFix(manager: LocationManager, provider: String): Coordinates? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(
                    manager,
                    provider,
                    signal,
                    ContextCompat.getMainExecutor(context),
                ) { location ->
                    val fix = location?.let { Coordinates(it.latitude, it.longitude) }
                    continuation.resume(fix)
                }
            } catch (exception: SecurityException) {
                continuation.resumeWithException(exception)
            } catch (exception: IllegalArgumentException) {
                continuation.resumeWithException(exception)
            }
        }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }
}
