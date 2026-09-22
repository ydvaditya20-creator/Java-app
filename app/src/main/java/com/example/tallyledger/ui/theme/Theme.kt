package com.example.tallyledger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerIndigo,
    onPrimaryContainer = OnPrimaryContainerIndigo,
    secondary = SecondarySky,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerSky,
    onSecondaryContainer = OnSecondaryContainerSky,
    tertiary = TertiaryEmerald,
    onTertiary = OnTertiaryWhite,
    tertiaryContainer = TertiaryContainerEmerald,
    onTertiaryContainer = OnTertiaryContainerEmerald,
    background = BackgroundSlate,
    onBackground = OnBackgroundSlate,
    surface = SurfaceCard,
    onSurface = OnSurfaceCard,
    surfaceVariant = SurfaceVariantSlate,
    onSurfaceVariant = OnSurfaceVariantSlate,
    outline = OutlineSlate,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryContainerIndigo,
    onPrimary = OnPrimaryContainerIndigo,
    primaryContainer = PrimaryIndigo,
    onPrimaryContainer = OnPrimaryWhite,
    secondary = SecondaryContainerSky,
    onSecondary = OnSecondaryContainerSky,
    background = OnBackgroundSlate,
    surface = OnSurfaceCard,
    onSurface = BackgroundSlate,
    surfaceVariant = OnSurfaceVariantSlate,
    onSurfaceVariant = SurfaceVariantSlate
)

@Composable
fun TallyLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
