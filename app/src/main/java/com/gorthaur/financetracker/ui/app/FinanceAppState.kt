package com.gorthaur.financetracker.ui.app

import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.FinanceAppPreferences
import com.gorthaur.financetracker.core.util.AppLanguageManager
import com.gorthaur.financetracker.data.local.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FinanceAppState(
    private val settingsDataStore: SettingsDataStore,
    private val scope: CoroutineScope
) {
    var preferences by mutableStateOf(FinanceAppPreferences())
        private set

    fun startObserving() {
        scope.launch {
            settingsDataStore.preferencesFlow.collectLatest { storedPreferences ->
                preferences = storedPreferences
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        // Aplica el idioma de inmediato (en el hilo principal, por la acción del
        // usuario). AppCompat lo persiste y lo restaura al reabrir la app, así que
        // NO lo aplicamos desde un efecto reactivo: hacerlo provocaba un bucle de
        // recreación (el valor por defecto reseteaba el locale en cada recreación).
        AppLanguageManager.applyLanguage(language)
        scope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun setThemePalette(palette: AppThemePalette) {
        scope.launch {
            settingsDataStore.setThemePalette(palette)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        scope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun setDefaultCurrency(currency: CurrencyCode) {
        scope.launch {
            settingsDataStore.setDefaultCurrency(currency)
        }
    }

    fun setAiApiKey(apiKey: String) {
        scope.launch {
            settingsDataStore.setAiApiKey(apiKey)
        }
    }
}