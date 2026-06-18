package com.gorthaur.financetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.core.util.Formatters
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.data.remote.ScanErrorReason
import com.gorthaur.financetracker.ui.components.CategoryPieChart
import com.gorthaur.financetracker.ui.components.LegendDot
import com.gorthaur.financetracker.ui.components.MonthlyBarChart
import com.gorthaur.financetracker.ui.components.SummaryCard
import com.gorthaur.financetracker.ui.components.TransactionRow
import com.gorthaur.financetracker.ui.components.rememberImagePickController
import com.gorthaur.financetracker.ui.screens.dashboard.DashboardFilter
import com.gorthaur.financetracker.ui.screens.dashboard.DashboardViewModel
import com.gorthaur.financetracker.ui.screens.transactions.ScanState
import com.gorthaur.financetracker.ui.screens.transactions.TransactionFormData
import com.gorthaur.financetracker.ui.screens.transactions.TransactionFormDialog
import com.gorthaur.financetracker.ui.screens.transactions.TransactionsViewModel
import java.util.Locale

private val IncomeColor = Color(0xFF2E7D32)
private val ExpenseColor = Color(0xFFC62828)

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    transactionsViewModel: TransactionsViewModel = viewModel()
) {
    val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val scanState by transactionsViewModel.scanState.collectAsStateWithLifecycle()

    var formData by remember { mutableStateOf<TransactionFormData?>(null) }
    var showActions by remember { mutableStateOf(false) }
    var showScanChooser by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val noKeyMessage = stringResource(R.string.scan_error_no_key)
    val networkMessage = stringResource(R.string.scan_error_network)
    val apiMessage = stringResource(R.string.scan_error_api)
    val parseMessage = stringResource(R.string.scan_error_parse)
    val imageMessage = stringResource(R.string.scan_error_image)

    val imagePicker = rememberImagePickController { uri ->
        transactionsViewModel.scanReceipt(uri)
    }

    // Reacciona al resultado del escaneo IA.
    LaunchedEffect(scanState) {
        when (val current = scanState) {
            is ScanState.Success -> {
                val scan = current.scan
                formData = TransactionFormData(
                    id = null,
                    type = scan.type,
                    title = scan.title,
                    amount = String.format(Locale.US, "%.2f", scan.amount),
                    currency = CurrencyCode.fromCode(scan.currencyCode) ?: state.currency,
                    category = scan.category,
                    dateMillis = scan.dateEpochMillis,
                    notes = scan.notes.orEmpty(),
                    photoPath = current.photoPath,
                    source = "AI_RECEIPT"
                )
                transactionsViewModel.consumeScan()
            }
            is ScanState.Error -> {
                scanError = when (current.reason) {
                    ScanErrorReason.NO_API_KEY -> noKeyMessage
                    ScanErrorReason.NETWORK -> networkMessage + (current.detail?.let { "\n$it" } ?: "")
                    ScanErrorReason.API -> apiMessage + (current.detail?.let { "\n$it" } ?: "")
                    ScanErrorReason.PARSE -> parseMessage
                    ScanErrorReason.NO_IMAGE -> imageMessage
                }
                transactionsViewModel.consumeScan()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = state.monthLabel.ifBlank { stringResource(R.string.dashboard_title) },
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        label = stringResource(R.string.dashboard_income),
                        amount = Formatters.money(state.monthIncome, state.currency),
                        accent = IncomeColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = stringResource(R.string.dashboard_expense),
                        amount = Formatters.money(state.monthExpense, state.currency),
                        accent = ExpenseColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                SummaryCard(
                    label = stringResource(R.string.dashboard_balance),
                    amount = Formatters.money(state.balance, state.currency),
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.expenseSlices.isNotEmpty()) {
                item {
                    SectionCard(title = stringResource(R.string.dashboard_by_category)) {
                        CategoryPieChart(slices = state.expenseSlices, currency = state.currency)
                    }
                }
            }

            if (state.monthlyBars.any { it.income > 0 || it.expense > 0 }) {
                item {
                    SectionCard(title = stringResource(R.string.dashboard_monthly)) {
                        MonthlyBarChart(bars = state.monthlyBars)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LegendDot(IncomeColor, stringResource(R.string.legend_income))
                            LegendDot(ExpenseColor, stringResource(R.string.legend_expense))
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.dashboard_recent),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter == DashboardFilter.ALL,
                        onClick = { dashboardViewModel.setFilter(DashboardFilter.ALL) },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                    FilterChip(
                        selected = state.filter == DashboardFilter.EXPENSE,
                        onClick = { dashboardViewModel.setFilter(DashboardFilter.EXPENSE) },
                        label = { Text(stringResource(R.string.filter_expenses)) }
                    )
                    FilterChip(
                        selected = state.filter == DashboardFilter.INCOME,
                        onClick = { dashboardViewModel.setFilter(DashboardFilter.INCOME) },
                        label = { Text(stringResource(R.string.filter_income)) }
                    )
                }
            }

            if (state.recent.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.dashboard_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.recent, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onClick = { formData = tx.toFormData() },
                        onDelete = { dashboardViewModel.deleteTransaction(tx) }
                    )
                    HorizontalDivider()
                }
            }

            item { Box(modifier = Modifier.padding(40.dp)) {} }
        }

        FloatingActionButton(
            onClick = { showActions = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_actions))
            DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.fab_scan)) },
                    onClick = {
                        showActions = false
                        showScanChooser = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.fab_add_expense)) },
                    onClick = {
                        showActions = false
                        formData = TransactionFormData(
                            type = TransactionType.EXPENSE,
                            currency = state.currency,
                            category = TransactionCategory.defaultFor(TransactionType.EXPENSE)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.fab_add_income)) },
                    onClick = {
                        showActions = false
                        formData = TransactionFormData(
                            type = TransactionType.INCOME,
                            currency = state.currency,
                            category = TransactionCategory.defaultFor(TransactionType.INCOME)
                        )
                    }
                )
            }
        }

        if (scanState is ScanState.Loading) {
            ScanLoadingOverlay()
        }
    }

    if (showScanChooser) {
        ScanSourceChooser(
            onCamera = {
                showScanChooser = false
                imagePicker.takePhoto()
            },
            onGallery = {
                showScanChooser = false
                imagePicker.pickFromGallery()
            },
            onDismiss = { showScanChooser = false }
        )
    }

    formData?.let { data ->
        TransactionFormDialog(
            initial = data,
            onDismiss = { formData = null },
            onSave = { result ->
                val amount = result.amount.replace(',', '.').toDoubleOrNull()
                if (amount != null) {
                    transactionsViewModel.saveTransaction(
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

    scanError?.let { message ->
        AlertDialog(
            onDismissRequest = { scanError = null },
            confirmButton = {
                TextButton(onClick = { scanError = null }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            title = { Text(stringResource(R.string.scan_error_title)) },
            text = { Text(message) }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Box(modifier = Modifier.padding(top = 12.dp)) { content() }
        }
    }
}

@Composable
private fun ScanSourceChooser(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.scan_choose_source)) },
        text = { Text(stringResource(R.string.scan_choose_hint)) },
        confirmButton = {
            TextButton(onClick = onCamera) { Text(stringResource(R.string.scan_camera)) }
        },
        dismissButton = {
            TextButton(onClick = onGallery) { Text(stringResource(R.string.scan_gallery)) }
        }
    )
}

@Composable
private fun ScanLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(stringResource(R.string.scan_loading))
            }
        }
    }
}

private fun TransactionEntity.toFormData(): TransactionFormData {
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
