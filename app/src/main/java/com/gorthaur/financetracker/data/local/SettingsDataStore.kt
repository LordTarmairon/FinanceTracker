package com.gorthaur.financetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.ExchangeRates
import com.gorthaur.financetracker.core.model.FinanceAppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_DATASTORE_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = SETTINGS_DATASTORE_NAME)

class SettingsDataStore (private val context: Context) {
    private object Keys{
        val language = stringPreferencesKey("language")
        val themePalette = stringPreferencesKey("theme_palette")
        val themeMode = stringPreferencesKey("theme_mode")
        val defaultCurrency = stringPreferencesKey("default_currency")
        val exchangeRates = stringPreferencesKey("exchange_rates")
        val aiApiKey = stringPreferencesKey("ai_api_key")
    }

    val preferencesFlow: Flow<FinanceAppPreferences> = context.dataStore.data.map { prefs ->
        FinanceAppPreferences(
            language = prefs[Keys.language]
                ?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.SYSTEM,
            themePalette = prefs[Keys.themePalette]
                ?.let { runCatching { AppThemePalette.valueOf(it) }.getOrNull() }
                ?: AppThemePalette.OCEAN,
            themeMode = prefs[Keys.themeMode]
                ?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                ?: AppThemeMode.SYSTEM,
            defaultCurrency = prefs[Keys.defaultCurrency]
                ?.let { CurrencyCode.fromCode(it) }
                ?: CurrencyCode.EUR,
            exchangeRates = prefs[Keys.exchangeRates]
                ?.let { deserializeRates(it) }
                ?: ExchangeRates(),
            aiApiKey = prefs[Keys.aiApiKey] ?: ""
        )
    }

    private fun deserializeRates(raw: String): ExchangeRates {
        // Formato "USD:1.08;CAD:1.47;KRW:1450.0" (el EUR siempre es 1.0).
        val map = ExchangeRates.DEFAULT.toMutableMap()
        raw.split(';').forEach { entry ->
            val parts = entry.split(':')
            if (parts.size == 2) {
                val currency = CurrencyCode.fromCode(parts[0])
                val value = parts[1].toDoubleOrNull()
                if (currency != null && value != null && value > 0) map[currency] = value
            }
        }
        return ExchangeRates(map)
    }

    private fun serializeRates(rates: ExchangeRates): String =
        rates.unitsPerEur
            .filterKeys { it != CurrencyCode.EUR }
            .entries
            .joinToString(";") { "${it.key.code}:${it.value}" }
    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.language] = language.name
        }
    }

    suspend fun setThemePalette(palette: AppThemePalette) {
        context.dataStore.edit { prefs ->
            prefs[Keys.themePalette] = palette.name
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.themeMode] = mode.name
        }
    }

    suspend fun setDefaultCurrency(currency: CurrencyCode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.defaultCurrency] = currency.code
        }
    }

    suspend fun setExchangeRates(rates: ExchangeRates) {
        context.dataStore.edit { prefs ->
            prefs[Keys.exchangeRates] = serializeRates(rates)
        }
    }

    suspend fun setAiApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.aiApiKey] = apiKey.trim()
        }
    }
}