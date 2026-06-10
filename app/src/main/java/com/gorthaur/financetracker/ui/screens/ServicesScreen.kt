package com.gorthaur.financetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.core.util.Formatters
import com.gorthaur.financetracker.data.local.entity.ServiceEntity
import com.gorthaur.financetracker.ui.screens.services.ServicesViewModel
import java.util.Locale

@Composable
fun ServicesScreen(
    viewModel: ServicesViewModel = viewModel()
) {
    val services by viewModel.services.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<ServiceFormData?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.services_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item {
                Text(
                    text = stringResource(R.string.service_monthly_total) + ": " +
                        Formatters.money(monthlyTotal, CurrencyCode.EUR),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (services.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.services_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(services, key = { it.id }) { service ->
                    ServiceCard(
                        service = service,
                        onClick = { editing = service.toFormData() },
                        onRegisterPayment = { viewModel.registerPayment(service) },
                        onDelete = { viewModel.deleteService(service) }
                    )
                }
            }
            item { Box(modifier = Modifier.padding(40.dp)) {} }
        }

        FloatingActionButton(
            onClick = { editing = ServiceFormData() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.services_add))
        }
    }

    editing?.let { data ->
        ServiceFormDialog(
            initial = data,
            onDismiss = { editing = null },
            onSave = { result ->
                val amount = result.amount.replace(',', '.').toDoubleOrNull()
                val day = result.billingDay.toIntOrNull() ?: 1
                if (result.name.isNotBlank() && amount != null) {
                    viewModel.saveService(
                        id = result.id,
                        name = result.name.trim(),
                        amount = amount,
                        currencyCode = result.currency.code,
                        category = result.category,
                        billingDayOfMonth = day,
                        active = result.active,
                        notes = result.notes.ifBlank { null }
                    )
                }
                editing = null
            }
        )
    }
}

@Composable
private fun ServiceCard(
    service: ServiceEntity,
    onClick: () -> Unit,
    onRegisterPayment: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.service_billing_day) + " " + service.billingDayOfMonth,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = Formatters.money(service.amount, service.currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!service.active) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.service_inactive)) })
                }
                TextButton(onClick = onRegisterPayment) {
                    Text(stringResource(R.string.service_register_payment))
                }
            }
        }
    }
}

data class ServiceFormData(
    val id: Long? = null,
    val name: String = "",
    val amount: String = "",
    val currency: CurrencyCode = CurrencyCode.EUR,
    val category: TransactionCategory = TransactionCategory.UTILITIES,
    val billingDay: String = "1",
    val active: Boolean = true,
    val notes: String = ""
)

private fun ServiceEntity.toFormData(): ServiceFormData = ServiceFormData(
    id = id,
    name = name,
    amount = String.format(Locale.US, "%.2f", amount),
    currency = CurrencyCode.fromCode(currencyCode) ?: CurrencyCode.EUR,
    category = TransactionCategory.fromKey(category, TransactionType.EXPENSE),
    billingDay = billingDayOfMonth.toString(),
    active = active,
    notes = notes.orEmpty()
)

@Composable
private fun ServiceFormDialog(
    initial: ServiceFormData,
    onDismiss: () -> Unit,
    onSave: (ServiceFormData) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var amount by remember { mutableStateOf(initial.amount) }
    var currency by remember { mutableStateOf(initial.currency) }
    var category by remember { mutableStateOf(initial.category) }
    var billingDay by remember { mutableStateOf(initial.billingDay) }
    var active by remember { mutableStateOf(initial.active) }
    var notes by remember { mutableStateOf(initial.notes) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.replace(',', '.').toDoubleOrNull()
    val canSave = name.isNotBlank() && amountValue != null && amountValue > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (initial.id == null) R.string.service_new_title else R.string.service_edit_title
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.service_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.service_amount)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = currency.code,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.tx_field_currency)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { currencyExpanded = true }
                        )
                        DropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            CurrencyCode.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("${option.code} (${option.symbol})") },
                                    onClick = {
                                        currency = option
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = stringResource(category.labelRes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.tx_field_category)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { categoryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        TransactionCategory.forType(TransactionType.EXPENSE).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.labelRes)) },
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = billingDay,
                    onValueChange = { input -> billingDay = input.filter { it.isDigit() }.take(2) },
                    label = { Text(stringResource(R.string.service_billing_day)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.service_active), modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.tx_field_notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        initial.copy(
                            name = name,
                            amount = amount,
                            currency = currency,
                            category = category,
                            billingDay = billingDay.ifBlank { "1" },
                            active = active,
                            notes = notes
                        )
                    )
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
