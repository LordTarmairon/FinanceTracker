package com.gorthaur.financetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette
import com.gorthaur.financetracker.core.model.CurrencyCode
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
            aiApiKey = prefs[Keys.aiApiKey] ?: ""
        )
    }
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

    suspend fun setAiApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.aiApiKey] = apiKey.trim()
        }
    }
}