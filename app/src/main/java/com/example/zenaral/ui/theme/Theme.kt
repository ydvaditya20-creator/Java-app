package com.example.zenaral.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val isDark: Boolean,
    val primaryHex: Long,
    val bgHex: Long
) {
    LIGHT(
        id = "light",
        title = "Clean Light",
        hindiTitle = "क्लीन लाइट (Default)",
        isDark = false,
        primaryHex = 0xFF4F46E5,
        bgHex = 0xFFF8FAFC
    ),
    CLASSIC_GREEN(
        id = "classic_green",
        title = "Classic Green",
        hindiTitle = "क्लासिक ग्रीन (Accounting)",
        isDark = false,
        primaryHex = 0xFF0D7A57,
        bgHex = 0xFFF2F9F5
    ),
    DARK(
        id = "dark",
        title = "Slate Dark",
        hindiTitle = "स्लेट डार्क मोड",
        isDark = true,
        primaryHex = 0xFF818CF8,
        bgHex = 0xFF0F172A
    ),
    AMOLED(
        id = "amoled",
        title = "AMOLED Black",
        hindiTitle = "एमोलेड ब्लैक (Battery Saver)",
        isDark = true,
        primaryHex = 0xFF10B981,
        bgHex = 0xFF000000
    ),
    ROYAL_BLUE(
        id = "royal_blue",
        title = "Royal Blue",
        hindiTitle = "रॉयल ब्लू (Banking)",
        isDark = false,
        primaryHex = 0xFF1D4ED8,
        bgHex = 0xFFF0F4FF
    ),
    WARM_AMBER(
        id = "warm_amber",
        title = "Warm Amber",
        hindiTitle = "वार्म एम्बर (Gold)",
        isDark = false,
        primaryHex = 0xFFD97706,
        bgHex = 0xFFFEFDF7
    );

    companion object {
        fun fromId(id: String?): AppTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: LIGHT
        }
    }
}

// 1. Clean Light Color Scheme
private val CleanLightColorScheme = lightColorScheme(
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

// 2. Classic Green Scheme
private val ClassicGreenColorScheme = lightColorScheme(
    primary = PrimaryClassicGreen,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerClassicGreen,
    onPrimaryContainer = OnPrimaryContainerClassicGreen,
    secondary = SecondarySky,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerSky,
    onSecondaryContainer = OnSecondaryContainerSky,
    tertiary = GoldVoucher,
    onTertiary = OnTertiaryWhite,
    background = BackgroundClassicGreen,
    onBackground = OnBackgroundClassicGreen,
    surface = SurfaceCard,
    onSurface = OnBackgroundClassicGreen,
    surfaceVariant = SurfaceVariantClassicGreen,
    onSurfaceVariant = OnSurfaceVariantClassicGreen,
    outline = OutlineClassicGreen,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

// 3. Dark Scheme
private val SlateDarkColorScheme = darkColorScheme(
    primary = PrimaryDarkIndigo,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryIndigo,
    onPrimaryContainer = OnPrimaryWhite,
    secondary = SecondarySky,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerSky,
    onSecondaryContainer = OnSecondaryContainerSky,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurface,
    outline = DarkOutline,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

// 4. AMOLED Black Scheme
private val AmoledColorScheme = darkColorScheme(
    primary = PrimaryAmoled,
    onPrimary = AmoledBlackBg,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = PrimaryAmoled,
    onSecondary = AmoledBlackBg,
    background = AmoledBlackBg,
    onBackground = AmoledOnSurface,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurface,
    outline = AmoledOutline,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

// 5. Royal Blue Scheme
private val RoyalBlueColorScheme = lightColorScheme(
    primary = PrimaryRoyalBlue,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerRoyalBlue,
    onPrimaryContainer = OnPrimaryContainerRoyalBlue,
    secondary = SecondarySky,
    onSecondary = OnSecondaryWhite,
    background = BackgroundRoyalBlue,
    onBackground = OnBackgroundSlate,
    surface = SurfaceCard,
    onSurface = OnSurfaceCard,
    surfaceVariant = SurfaceVariantSlate,
    onSurfaceVariant = OnSurfaceVariantSlate,
    outline = OutlineRoyalBlue,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

// 6. Warm Amber Scheme
private val WarmAmberColorScheme = lightColorScheme(
    primary = PrimaryWarmAmber,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerWarmAmber,
    onPrimaryContainer = OnPrimaryContainerWarmAmber,
    secondary = GoldVoucher,
    onSecondary = OnSecondaryWhite,
    background = BackgroundWarmAmber,
    onBackground = OnBackgroundSlate,
    surface = SurfaceCard,
    onSurface = OnSurfaceCard,
    surfaceVariant = SurfaceVariantWarmAmber,
    onSurfaceVariant = OnSurfaceVariantSlate,
    outline = OutlineWarmAmber,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

@Composable
fun ZenaralTheme(
    themeMode: AppTheme = AppTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeMode) {
        AppTheme.LIGHT -> CleanLightColorScheme
        AppTheme.CLASSIC_GREEN -> ClassicGreenColorScheme
        AppTheme.DARK -> SlateDarkColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
        AppTheme.ROYAL_BLUE -> RoyalBlueColorScheme
        AppTheme.WARM_AMBER -> WarmAmberColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
