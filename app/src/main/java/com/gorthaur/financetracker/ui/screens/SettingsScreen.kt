package com.gorthaur.financetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.AppLanguage
import com.gorthaur.financetracker.core.model.AppThemeMode
import com.gorthaur.financetracker.core.model.AppThemePalette
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.util.labelRes
import com.gorthaur.financetracker.ui.app.FinanceAppState

@Composable
fun SettingsScreen(appState: FinanceAppState) {
    val prefs = appState.preferences

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(text = stringResource(R.string.settings_subtitle))

        LanguageSelector(
            current = prefs.language,
            onSelected = { appState.setLanguage(it) }
        )

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

        HorizontalDivider()

        // Moneda por defecto
        CurrencySelector(
            current = prefs.defaultCurrency,
            onSelected = { appState.setDefaultCurrency(it) }
        )

        HorizontalDivider()

        // Tipos de cambio (para convertir las estadísticas)
        ExchangeRatesSection(appState = appState)

        HorizontalDivider()

        // Clave de la API para el escaneo de tickets con IA
        ApiKeySection(
            currentKey = prefs.aiApiKey,
            onSave = { appState.setAiApiKey(it) }
        )
    }
}

@Composable
private fun LanguageSelector(
    current: AppLanguage,
    onSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Text(
        text = stringResource(R.string.settings_language),
        style = MaterialTheme.typography.titleMedium
    )
    Box {
        OutlinedTextField(
            value = stringResource(current.labelRes()),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes())) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExchangeRatesSection(appState: FinanceAppState) {
    val rates = appState.preferences.exchangeRates
    val currencies = CurrencyCode.entries.filter { it != CurrencyCode.EUR }
    // Re-siembra los campos cuando cambian los tipos guardados (p. ej. tras actualizar).
    val ratesKey = currencies.joinToString { "${it.code}:${rates.unitsPerEur(it)}" }
    val texts = remember(ratesKey) {
        mutableStateMapOf<CurrencyCode, String>().apply {
            currencies.forEach { put(it, formatRate(rates.unitsPerEur(it))) }
        }
    }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val updatedMsg = stringResource(R.string.settings_exchange_updated)
    val failedMsg = stringResource(R.string.settings_exchange_failed)

    Text(
        text = stringResource(R.string.settings_exchange_rates),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = stringResource(R.string.settings_exchange_rates_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    currencies.forEach { currency ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.exchange_per_euro))
            OutlinedTextField(
                value = texts[currency].orEmpty(),
                onValueChange = { texts[currency] = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Text("${currency.code} (${currency.symbol})")
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            currencies.forEach { c ->
                texts[c]?.replace(',', '.')?.toDoubleOrNull()?.let { v ->
                    if (v > 0) appState.setExchangeRate(c, v)
                }
            }
            message = updatedMsg
        }) {
            Text(stringResource(R.string.action_save))
        }
        OutlinedButton(
            enabled = !loading,
            onClick = {
                loading = true
                message = null
                appState.refreshExchangeRates { ok ->
                    loading = false
                    message = if (ok) updatedMsg else failedMsg
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.settings_exchange_update))
            }
        }
    }

    message?.let {
        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}

private fun formatRate(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(java.util.Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')

@Composable
private fun CurrencySelector(
    current: CurrencyCode,
    onSelected: (CurrencyCode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Text(
        text = stringResource(R.string.settings_currency),
        style = MaterialTheme.typography.titleMedium
    )
    Box {
        OutlinedTextField(
            value = "${current.code} (${current.symbol})",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CurrencyCode.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${option.code} (${option.symbol})") },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ApiKeySection(
    currentKey: String,
    onSave: (String) -> Unit
) {
    var keyInput by remember(currentKey) { mutableStateOf(currentKey) }

    Text(
        text = stringResource(R.string.settings_api_key),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = stringResource(R.string.settings_api_key_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = keyInput,
        onValueChange = { keyInput = it },
        label = { Text(stringResource(R.string.settings_api_key)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { onSave(keyInput) }) {
        Text(stringResource(R.string.action_save))
    }
}
