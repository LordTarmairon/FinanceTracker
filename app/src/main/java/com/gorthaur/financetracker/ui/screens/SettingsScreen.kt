package com.gorthaur.financetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette
import com.gorthaur.financetracker.core.util.labelRes
import com.gorthaur.financetracker.ui.app.FinanceAppState

@Composable
fun SettingsScreen(appState: FinanceAppState) {
    val prefs = appState.preferences

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(text = stringResource(R.string.settings_subtitle))

        Text(
            text = "${stringResource(R.string.settings_language)}: ${stringResource(prefs.language.labelRes())}"
        )
        Button(
            onClick = {
                val next = when (prefs.language) {
                    AppLanguage.SYSTEM -> AppLanguage.ENGLISH
                    AppLanguage.ENGLISH -> AppLanguage.SPANISH
                    AppLanguage.SPANISH -> AppLanguage.SYSTEM
                    AppLanguage.KOREAN -> AppLanguage.KOREAN

                }
                appState.setLanguage(next)
            }
        ) {
            Text(stringResource(R.string.action_change_language))
        }

        Text(
            text = "${stringResource(R.string.settings_theme)}: ${stringResource(prefs.themePalette.labelRes())}"
        )
        Button(
            onClick = {
                val next = when (prefs.themePalette) {
                    AppThemePalette.OCEAN -> AppThemePalette.FOREST
                    AppThemePalette.FOREST -> AppThemePalette.ROYAL
                    AppThemePalette.ROYAL -> AppThemePalette.OCEAN
                }
                appState.setThemePalette(next)
            }
        ) {
            Text(stringResource(R.string.action_change_theme))
        }

        Text(
            text = "${stringResource(R.string.settings_dark_mode)}: ${stringResource(prefs.themeMode.labelRes())}"
        )
        Button(
            onClick = {
                val next = when (prefs.themeMode) {
                    AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                    AppThemeMode.LIGHT -> AppThemeMode.DARK
                    AppThemeMode.DARK -> AppThemeMode.SYSTEM
                }
                appState.setThemeMode(next)
            }
        ) {
            Text(stringResource(R.string.action_change_appearance))
        }
    }
}