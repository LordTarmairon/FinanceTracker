package com.gorthaur.financetracker.core.model

data class FinanceAppPreferences (
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themePalette: AppThemePalette = AppThemePalette.OCEAN,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)