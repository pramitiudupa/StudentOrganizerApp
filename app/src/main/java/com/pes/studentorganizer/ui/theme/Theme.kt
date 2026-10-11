package com.pes.studentorganizer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Navy40,
    primaryContainer = NavyContainer90,
    onPrimaryContainer = OnNavyContainer10,
    secondary = Teal40,
    secondaryContainer = TealContainer90,
    tertiary = Amber40,
    tertiaryContainer = AmberContainer90,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = Navy80,
    onPrimary = OnNavy20,
    primaryContainer = NavyContainer30,
    onPrimaryContainer = OnNavyContainer90,
    secondary = Teal80,
    tertiary = Amber80,
    background = DarkBackground,
    surface = DarkBackground,
    surfaceVariant = DarkSurfaceVariant
)

// darkTheme is passed in from MainActivity, so the Dark Mode Switch
// on the Settings screen can change the theme while the app is running.
@Composable
fun StudentOrganizerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // SideEffect runs after every successful recomposition.
    // It keeps the status bar / navigation bar icons readable
    // (dark icons on the light theme, light icons on the dark theme).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
