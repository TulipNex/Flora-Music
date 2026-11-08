package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FloraColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    secondary = EmeraldLight,
    tertiary = EmeraldDark,
    background = DarkBackground,
    surface = SurfaceSlate,
    onPrimary = SoftWhite,
    onSecondary = DarkBackground,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
    surfaceVariant = CardSlate,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark-first theme for premium music player feel
    dynamicColor: Boolean = false, // Use our hand-crafted theme for cohesive branding
    content: @Composable () -> Unit,
) {
    val colorScheme = FloraColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
