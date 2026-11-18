package com.leonardo.valderas.meeklyweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.leonardo.valderas.meeklyweather.ui.presentation.WeeklyWeatherViewModel
import com.leonardo.valderas.meeklyweather.ui.screen.WeeklyWeatherRoute
import com.leonardo.valderas.meeklyweather.ui.theme.TrackforceMeeklyWeatherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: WeeklyWeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackforceMeeklyWeatherTheme {
                WeeklyWeatherRoute(viewModel)
            }
        }
    }
}
