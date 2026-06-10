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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.gorthaur.financetracker.ui.screens.shopping.ShoppingViewModel

@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingViewModel = viewModel()
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    var selectedListId by remember { mutableStateOf<Long?>(null) }
    val selectedList = lists.firstOrNull { it.id == selectedListId }

    if (selectedList == null) {
        ShoppingListsOverview(
            lists = lists,
            onOpen = { selectedListId = it.id },
            onCreate = { name, desc -> viewModel.createList(name, desc) },
            onDelete = { viewModel.deleteList(it.id) }
        )
    } else {
        ShoppingListDetail(
            list = selectedList,
            viewModel = viewModel,
            onBack = { selectedListId = null }
        )
    }
}

@Composable
private fun ShoppingListsOverview(
    lists: List<ShoppingListEntity>,
    onOpen: (ShoppingListEntity) -> Unit,
    onCreate: (String, String) -> Unit,
    onDelete: (ShoppingListEntity) -> Unit
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
            if (lists.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.shopping_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(lists, key = { it.id }) { list ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(list) }) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = list.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (list.description.isNotBlank()) {
                                    Text(
                                        text = list.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = Formatters.date(list.dateEpochMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDelete(list) }) {
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
    onBack: () -> Unit
) {
    val items by viewModel.observeItems(list.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddItem by remember { mutableStateOf(false) }
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberImagePickController { uri -> pendingPhoto = uri }
    val currency = CurrencyCode.fromCode(list.currencyCode) ?: CurrencyCode.EUR
    val total = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity }

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
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.shopping_total) + ": " +
                        Formatters.money(total, currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
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
                        onDelete = { viewModel.deleteItem(item.id) }
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
        AddItemDialog(
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
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    currency: CurrencyCode,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.photoUri != null) {
                AsyncImage(
                    model = java.io.File(item.photoUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
            ) {
                Text(text = item.name, fontWeight = FontWeight.Medium)
                Text(
                    text = "x${item.quantity}" + (item.unitPrice?.let {
                        " · " + Formatters.money(it, currency)
                    } ?: ""),
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
private fun AddItemDialog(
    photo: Uri?,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Double?, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shopping_item_add)) },
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
