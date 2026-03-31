package com.gorthaur.financetracker.ui.app

import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette
import com.gorthaur.financetracker.core.model.FinanceAppPreferences
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
}