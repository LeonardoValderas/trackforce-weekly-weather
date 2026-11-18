package com.leonardo.valderas.meeklyweather.ui.screen

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leonardo.valderas.meeklyweather.ui.presentation.WeeklyWeatherViewModel

@Composable
fun WeeklyWeatherRoute(viewModel: WeeklyWeatherViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences = remember(context) {
        context.getSharedPreferences("location_permission", Context.MODE_PRIVATE)
    }
    var permissionRequestPending by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionRequestPending = false
        viewModel.refresh(granted)
    }

    LaunchedEffect(viewModel) {
        val granted = context.hasLocationPermission()
        val shouldRequest = !granted && !permissionRequestPending &&
            !preferences.getBoolean(PERMISSION_PROMPT_ATTEMPTED, false)
        viewModel.onScreenOpened(
            hasLocationPermission = granted,
            awaitingPermission = !granted && (shouldRequest || permissionRequestPending),
        )
        if (shouldRequest) {
            preferences.edit { putBoolean(PERMISSION_PROMPT_ATTEMPTED, true) }
            permissionRequestPending = true
            permissionLauncher.launch(ACCESS_COARSE_LOCATION)
        }
    }

    WeeklyWeatherScreen(state = state, onRefresh = {
        viewModel.refresh(context.hasLocationPermission())
    })
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

private const val PERMISSION_PROMPT_ATTEMPTED = "permission_prompt_attempted"
