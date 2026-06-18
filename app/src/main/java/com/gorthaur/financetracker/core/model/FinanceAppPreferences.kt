package com.gorthaur.financetracker.core.model

data class FinanceAppPreferences (
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themePalette: AppThemePalette = AppThemePalette.OCEAN,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val defaultCurrency: CurrencyCode = CurrencyCode.EUR,
    /** Tipos de cambio respecto al euro para convertir las estadísticas. */
    val exchangeRates: ExchangeRates = ExchangeRates(),
    /** Clave de la API de Claude para el escaneo de tickets con IA. */
    val aiApiKey: String = ""
)
