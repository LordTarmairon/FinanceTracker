package com.gorthaur.financetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private fun oceanLightScheme() = lightColorScheme(
    primary = OceanPrimaryLight,
    secondary = OceanSecondaryLight,
    background = OceanBackgroundLight,
    surface = OceanBackgroundLight
)

private fun oceanDarkScheme() = darkColorScheme(
    primary = OceanPrimaryDark,
    secondary = OceanSecondaryDark,
    background = OceanBackgroundDark,
    surface = OceanBackgroundDark
)

private fun forestLightScheme() = lightColorScheme(
    primary = ForestPrimaryLight,
    secondary = ForestSecondaryLight,
    background = ForestBackgroundLight,
    surface = ForestBackgroundLight
)

private fun forestDarkScheme() = darkColorScheme(
    primary = ForestPrimaryDark,
    secondary = ForestSecondaryDark,
    background = ForestBackgroundDark,
    surface = ForestBackgroundDark
)

private fun royalLightScheme() = lightColorScheme(
    primary = RoyalPrimaryLight,
    secondary = RoyalSecondaryLight,
    background = RoyalBackgroundLight,
    surface = RoyalBackgroundLight
)

private fun royalDarkScheme() = darkColorScheme(
    primary = RoyalPrimaryDark,
    secondary = RoyalSecondaryDark,
    background = RoyalBackgroundDark,
    surface = RoyalBackgroundDark
)

@Composable
fun FinanceTrackerTheme(
    palette: AppThemePalette = AppThemePalette.OCEAN,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when (palette) {
        AppThemePalette.OCEAN -> if (useDarkTheme) oceanDarkScheme() else oceanLightScheme()
        AppThemePalette.FOREST -> if (useDarkTheme) forestDarkScheme() else forestLightScheme()
        AppThemePalette.ROYAL -> if (useDarkTheme) royalDarkScheme() else royalLightScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}