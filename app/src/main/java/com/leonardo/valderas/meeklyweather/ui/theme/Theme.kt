package com.leonardo.valderas.meeklyweather.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TrackforceCyan,
    onPrimary = TrackforceNavy,
    primaryContainer = TrackforceDeepBlue,
    onPrimaryContainer = Color(0xFFE7F4FF),
    secondaryContainer = Color(0xFF183B5B),
    onSecondaryContainer = Color(0xFFE7F4FF),
    tertiaryContainer = TrackforceLime,
    onTertiaryContainer = TrackforceNavy,
    background = TrackforceNavy,
    onBackground = Color(0xFFF3F8FF),
    surface = TrackforceNavy,
    onSurface = Color(0xFFF3F8FF),
    surfaceContainerLow = Color(0xFF102541),
    onSurfaceVariant = Color(0xFFAEC7DE),
    outline = Color(0xFF809AB5),
    outlineVariant = Color(0xFF2E4F70),
    errorContainer = Color(0xFF40212D),
    onErrorContainer = Color(0xFFFFE6EB),
)

private val LightColorScheme = lightColorScheme(
    primary = TrackforceBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCF3FC),
    onPrimaryContainer = TrackforceNavy,
    secondaryContainer = Color(0xFFDAECFA),
    onSecondaryContainer = TrackforceNavy,
    tertiaryContainer = TrackforceLime,
    onTertiaryContainer = TrackforceNavy,
    background = Color(0xFFF3F7FB),
    onBackground = TrackforceNavy,
    surface = Color.White,
    onSurface = TrackforceNavy,
    surfaceContainerLow = Color.White,
    onSurfaceVariant = Color(0xFF425974),
    outline = Color(0xFF6C849D),
    outlineVariant = Color(0xFFCADCEC),
    errorContainer = Color(0xFFFFEAE8),
    onErrorContainer = Color(0xFF59222C),
)

@Composable
fun TrackforceMeeklyWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
