package com.gorthaur.financetracker.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gorthaur.financetracker.R
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.util.Formatters
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingListEntity
import com.gorthaur.financetracker.ui.components.rememberImagePickController
import com.gorthaur.financetracker.ui.screens.shopping.ShoppingListSummary
import com.gorthaur.financetracker.ui.screens.shopping.ShoppingViewModel
import java.util.Locale

private val CompletedColor = Color(0xFF2E7D32)

@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingViewModel = viewModel()
) {
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    var selectedListId by remember { mutableStateOf<Long?>(null) }
    val selected = summaries.firstOrNull { it.list.id == selectedListId }

    if (selected == null) {
        ShoppingListsOverview(
            summaries = summaries,
            onOpen = { selectedListId = it.list.id },
            onCreate = { name, desc -> viewModel.createList(name, desc) },
            onCopy = { viewModel.copyList(it.list) },
            onDelete = { viewModel.deleteList(it.list) }
        )
    } else {
        ShoppingListDetail(
            list = selected.list,
            viewModel = viewModel,
            onBack = { selectedListId = null },
            onCopy = { viewModel.copyList(selected.list) }
        )
    }
}

@Composable
private fun ShoppingListsOverview(
    summaries: List<ShoppingListSummary>,
    onOpen: (ShoppingListSummary) -> Unit,
    onCreate: (String, String) -> Unit,
    onCopy: (ShoppingListSummary) -> Unit,
    onDelete: (ShoppingListSummary) -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.shopping_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            if (summaries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.shopping_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(summaries, key = { it.list.id }) { summary ->
                    val currency = CurrencyCode.fromCode(summary.list.currencyCode) ?: CurrencyCode.EUR
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(summary) }) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = summary.list.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (summary.list.completed) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = stringResource(R.string.shopping_completed),
                                            tint = CompletedColor,
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .size(18.dp)
                                        )
                                    }
                                }
                                if (summary.list.description.isNotBlank()) {
                                    Text(
                                        text = summary.list.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = stringResource(
                                        R.string.shopping_items_count,
                                        summary.purchasedCount,
                                        summary.itemCount
                                    ) + " · " + Formatters.money(summary.total, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { onCopy(summary) }) {
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = stringResource(R.string.shopping_copy)
                                )
                            }
                            IconButton(onClick = { onDelete(summary) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
            item { Box(modifier = Modifier.padding(40.dp)) {} }
        }

        FloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.shopping_add_list))
        }
    }

    if (showCreate) {
        CreateListDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, desc ->
                onCreate(name, desc)
                showCreate = false
            }
        )
    }
}

@Composable
private fun CreateListDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shopping_add_list)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.shopping_list_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.shopping_list_desc)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name.trim(), description.trim()) }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun ShoppingListDetail(
    list: ShoppingListEntity,
    viewModel: ShoppingViewModel,
    onBack: () -> Unit,
    onCopy: () -> Unit
) {
    val items by viewModel.observeItems(list.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddItem by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItemEntity?>(null) }
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }
    var taxInput by remember(list.id) { mutableStateOf(formatAmount(list.taxAmount)) }

    val imagePicker = rememberImagePickController { uri -> pendingPhoto = uri }
    val currency = CurrencyCode.fromCode(list.currencyCode) ?: CurrencyCode.EUR
    val itemsTotal = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
    val total = itemsTotal + list.taxAmount
    val purchasedCount = items.count { it.purchased }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.shopping_copy)
                        )
                    }
                }
            }

            if (list.completed) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CompletedColor.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = CompletedColor)
                            Text(
                                text = stringResource(R.string.shopping_completed_banner),
                                modifier = Modifier.padding(start = 8.dp),
                                color = CompletedColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.shopping_progress, purchasedCount, items.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (items.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.shopping_empty_items),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        currency = currency,
                        onToggle = { viewModel.togglePurchased(item) },
                        onEdit = { editingItem = item },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
            }

            // Impuestos no incluidos en el precio
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        label = { Text(stringResource(R.string.shopping_taxes)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        viewModel.setTax(list, taxInput.replace(',', '.').toDoubleOrNull() ?: 0.0)
                    }) {
                        Text(stringResource(R.string.action_apply))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.shopping_total),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = Formatters.money(total, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { Box(modifier = Modifier.padding(40.dp)) {} }
        }

        FloatingActionButton(
            onClick = {
                pendingPhoto = null
                showAddItem = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.shopping_item_add))
        }
    }

    if (showAddItem) {
        ItemDialog(
            titleRes = R.string.shopping_item_add,
            initialName = "",
            initialPrice = "",
            initialQuantity = "1",
            photo = pendingPhoto,
            onPickCamera = { imagePicker.takePhoto() },
            onPickGallery = { imagePicker.pickFromGallery() },
            onDismiss = {
                showAddItem = false
                pendingPhoto = null
            },
            onSave = { name, price, qty ->
                viewModel.addItem(list.id, name, price, qty, pendingPhoto)
                showAddItem = false
                pendingPhoto = null
            }
        )
    }

    editingItem?.let { item ->
        ItemDialog(
            titleRes = R.string.shopping_item_edit,
            initialName = item.name,
            initialPrice = item.unitPrice?.let { formatAmount(it) } ?: "",
            initialQuantity = item.quantity.toString(),
            photo = null,
            onPickCamera = null,
            onPickGallery = null,
            onDismiss = { editingItem = null },
            onSave = { name, price, qty ->
                viewModel.updateItem(item, name, price, qty)
                editingItem = null
            }
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    currency: CurrencyCode,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.purchased, onCheckedChange = { onToggle() })

            if (item.photoUri != null) {
                AsyncImage(
                    model = java.io.File(item.photoUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable(onClick = onEdit)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.purchased) TextDecoration.LineThrough else null,
                    color = if (item.purchased) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "x${item.quantity}" + (item.unitPrice?.let {
                        " · " + Formatters.money(it, currency)
                    } ?: " · " + stringResource(R.string.shopping_no_price)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = Formatters.money((item.unitPrice ?: 0.0) * item.quantity, currency),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ItemDialog(
    titleRes: Int,
    initialName: String,
    initialPrice: String,
    initialQuantity: String,
    photo: Uri?,
    onPickCamera: (() -> Unit)?,
    onPickGallery: (() -> Unit)?,
    onDismiss: () -> Unit,
    onSave: (String, Double?, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }
    var quantity by remember { mutableStateOf(initialQuantity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.shopping_item_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(stringResource(R.string.shopping_item_price)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { input -> quantity = input.filter { it.isDigit() }.take(3) },
                        label = { Text(stringResource(R.string.shopping_item_qty)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (onPickCamera != null && onPickGallery != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (photo != null) {
                            AsyncImage(
                                model = photo,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        TextButton(onClick = onPickCamera) { Text(stringResource(R.string.scan_camera)) }
                        TextButton(onClick = onPickGallery) { Text(stringResource(R.string.scan_gallery)) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        name.trim(),
                        price.replace(',', '.').toDoubleOrNull(),
                        quantity.toIntOrNull() ?: 1
                    )
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

private fun formatAmount(value: Double): String =
    if (value == 0.0) "" else String.format(Locale.US, "%.2f", value)
