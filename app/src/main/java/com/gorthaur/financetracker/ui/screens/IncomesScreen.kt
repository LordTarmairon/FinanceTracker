package com.gorthaur.financetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.ui.components.TransactionRow
import com.gorthaur.financetracker.ui.screens.transactions.TransactionFormData
import com.gorthaur.financetracker.ui.screens.transactions.TransactionFormDialog
import com.gorthaur.financetracker.ui.screens.transactions.TransactionsViewModel
import java.util.Locale

@Composable
fun IncomesScreen(
    viewModel: TransactionsViewModel = viewModel()
) {
    val incomes by viewModel.incomes.collectAsStateWithLifecycle()
    val currency by viewModel.defaultCurrency.collectAsStateWithLifecycle()
    var formData by remember { mutableStateOf<TransactionFormData?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.incomes_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (incomes.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.incomes_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(incomes, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onClick = { formData = tx.toIncomeFormData() },
                        onDelete = { viewModel.deleteTransaction(tx) }
                    )
                    HorizontalDivider()
                }
            }
            item { Box(modifier = Modifier.padding(40.dp)) {} }
        }

        FloatingActionButton(
            onClick = {
                formData = TransactionFormData(
                    type = TransactionType.INCOME,
                    currency = currency,
                    category = TransactionCategory.defaultFor(TransactionType.INCOME)
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.incomes_add))
        }
    }

    formData?.let { data ->
        TransactionFormDialog(
            initial = data,
            onDismiss = { formData = null },
            onSave = { result ->
                val amount = result.amount.replace(',', '.').toDoubleOrNull()
                if (amount != null) {
                    viewModel.saveTransaction(
                        id = result.id,
                        type = result.type,
                        title = result.title,
                        amount = amount,
                        currencyCode = result.currency.code,
                        category = result.category,
                        dateEpochMillis = result.dateMillis,
                        notes = result.notes.ifBlank { null },
                        photoPath = result.photoPath,
                        source = result.source
                    )
                }
                formData = null
            }
        )
    }
}

private fun TransactionEntity.toIncomeFormData(): TransactionFormData {
    val type = TransactionType.fromName(type)
    return TransactionFormData(
        id = id,
        type = type,
        title = title,
        amount = String.format(Locale.US, "%.2f", amount),
        currency = CurrencyCode.fromCode(currencyCode) ?: CurrencyCode.EUR,
        category = TransactionCategory.fromKey(category, type),
        dateMillis = dateEpochMillis,
        notes = notes.orEmpty(),
        photoPath = photoUri,
        source = source
    )
}
