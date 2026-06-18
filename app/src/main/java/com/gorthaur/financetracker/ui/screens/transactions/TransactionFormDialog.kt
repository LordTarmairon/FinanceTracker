package com.gorthaur.financetracker.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.core.util.Formatters

data class TransactionFormData(
    val id: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val title: String = "",
    val amount: String = "",
    val currency: CurrencyCode = CurrencyCode.EUR,
    val category: TransactionCategory = TransactionCategory.OTHER_EXPENSE,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val photoPath: String? = null,
    val source: String = "MANUAL"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    initial: TransactionFormData,
    onDismiss: () -> Unit,
    onSave: (TransactionFormData) -> Unit
) {
    var type by remember { mutableStateOf(initial.type) }
    var title by remember { mutableStateOf(initial.title) }
    var amount by remember { mutableStateOf(initial.amount) }
    var currency by remember { mutableStateOf(initial.currency) }
    var category by remember {
        mutableStateOf(
            if (initial.category.type == initial.type) initial.category
            else TransactionCategory.defaultFor(initial.type)
        )
    }
    var dateMillis by remember { mutableStateOf(initial.dateMillis) }
    var notes by remember { mutableStateOf(initial.notes) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amountValue = amount.replace(',', '.').toDoubleOrNull()
    val canSave = title.isNotBlank() && amountValue != null && amountValue > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (initial.id == null) R.string.tx_new_title else R.string.tx_edit_title
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            type = TransactionType.EXPENSE
                            category = TransactionCategory.defaultFor(type)
                        },
                        label = { Text(stringResource(R.string.tx_type_expense)) }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            category = TransactionCategory.defaultFor(type)
                        },
                        label = { Text(stringResource(R.string.tx_type_income)) }
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.tx_field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.tx_field_amount)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = stringResource(R.string.tx_field_currency),
                        value = "${currency.code} (${currency.symbol})",
                        options = CurrencyCode.entries,
                        optionLabel = { "${it.code} (${it.symbol})" },
                        onSelected = { currency = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                DropdownField(
                    label = stringResource(R.string.tx_field_category),
                    value = stringResource(category.labelRes),
                    options = TransactionCategory.forType(type),
                    optionLabel = { stringResource(it.labelRes) },
                    onSelected = { category = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = Formatters.date(dateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.tx_field_date)) },
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(stringResource(R.string.tx_pick_date))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

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
                            type = type,
                            title = title.trim(),
                            amount = amount,
                            currency = currency,
                            category = category,
                            dateMillis = dateMillis,
                            notes = notes
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** Campo de solo lectura que despliega un menú con [options] al pulsarlo. */
@Composable
private fun <T> DropdownField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que captura la pulsación sobre el campo de solo lectura.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
