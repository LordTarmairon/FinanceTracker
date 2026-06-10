package com.gorthaur.financetracker.core.model

data class FinanceAppPreferences (
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themePalette: AppThemePalette = AppThemePalette.OCEAN,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val defaultCurrency: CurrencyCode = CurrencyCode.EUR,
    /** Clave de la API de Claude para el escaneo de tickets con IA. */
    val aiApiKey: String = ""
)
