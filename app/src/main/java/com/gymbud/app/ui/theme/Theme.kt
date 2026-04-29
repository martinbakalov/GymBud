package com.gymbud.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandBlueOn,
    primaryContainer = BrandBlue,
    onPrimaryContainer = BrandBlueOn,

    background = Paper,
    onBackground = Ink,

    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = InkMuted,

    outline = HairlineLight,
    outlineVariant = HairlineLight,

    secondary = Ink,
    onSecondary = Paper
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = BrandBlueOn,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = BrandBlueOn,

    background = InkDark,
    onBackground = PaperOnDark,

    surface = InkDark,
    onSurface = PaperOnDark,
    surfaceVariant = InkDarkElev,
    onSurfaceVariant = PaperOnDarkMuted,

    outline = HairlineDark,
    outlineVariant = HairlineDark,

    secondary = PaperOnDark,
    onSecondary = InkDark
)

@Composable
fun GymBudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(darkTheme) {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}