package com.gorthaur.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.gorthaur.financetracker.core.util.AppLanguageManager
import com.gorthaur.financetracker.data.local.SettingsDataStore
import com.gorthaur.financetracker.ui.app.FinanceApp
import com.gorthaur.financetracker.ui.app.FinanceAppState

import com.gorthaur.financetracker.ui.theme.FinanceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val settingsDataStore = remember { SettingsDataStore(applicationContext) }
            val appState = remember { FinanceAppState(settingsDataStore, scope) }

            LaunchedEffect(Unit) {
                appState.startObserving()
            }

            LaunchedEffect(appState.preferences.language) {
                AppLanguageManager.applyLanguage(appState.preferences.language)
            }

            FinanceTrackerTheme(
                palette = appState.preferences.themePalette,
                themeMode = appState.preferences.themeMode
            ) {
                FinanceApp(appState = appState)
            }
        }
    }
}
