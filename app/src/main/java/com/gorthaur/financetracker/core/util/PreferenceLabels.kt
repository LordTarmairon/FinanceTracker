package com.gorthaur.financetracker.core.util

import androidx.annotation.StringRes
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette

@StringRes
fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.theme_mode_system
    AppLanguage.ENGLISH -> R.string.language_english
    AppLanguage.SPANISH -> R.string.language_spanish
    AppLanguage.KOREAN -> R.string.language_korean
}
@StringRes
fun AppThemePalette.labelRes(): Int = when (this) {
    AppThemePalette.OCEAN -> R.string.theme_ocean
    AppThemePalette.FOREST -> R.string.theme_forest
    AppThemePalette.ROYAL -> R.string.theme_royal
}

@StringRes
fun AppThemeMode.labelRes(): Int = when (this) {
    AppThemeMode.SYSTEM -> R.string.theme_mode_system
    AppThemeMode.LIGHT -> R.string.theme_mode_light
    AppThemeMode.DARK -> R.string.theme_mode_dark
}