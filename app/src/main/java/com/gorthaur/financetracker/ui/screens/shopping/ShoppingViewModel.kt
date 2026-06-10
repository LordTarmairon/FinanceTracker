package com.gorthaur.financetracker.ui.screens.shopping

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorthaur.financetracker.core.util.ImageUtils
import com.gorthaur.financetracker.data.local.SettingsDataStore
import com.gorthaur.financetracker.data.local.database.DatabaseProvider
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingListEntity
import com.gorthaur.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

class ShoppingViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FinanceRepository(DatabaseProvider.getDatabase(app))
    private val settings = SettingsDataStore(app)

    val lists: StateFlow<List<ShoppingListEntity>> =
        repository.observeShoppingLists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeItems(listId: Long): Flow<List<ShoppingItemEntity>> =
        repository.observeShoppingItems(listId)

    fun createList(name: String, description: String) {
        viewModelScope.launch {
            val currency = settings.preferencesFlow.first().defaultCurrency
            repository.addShoppingList(
                ShoppingListEntity(
                    name = name,
                    description = description,
                    dateEpochMillis = Instant.now().toEpochMilli(),
                    currencyCode = currency.code
                )
            )
        }
    }

    fun deleteList(id: Long) {
        viewModelScope.launch { repository.deleteShoppingList(id) }
    }

    fun addItem(
        listId: Long,
        name: String,
        unitPrice: Double?,
        quantity: Int,
        photoSource: Uri?
    ) {
        viewModelScope.launch {
            val photoPath = photoSource?.let {
                ImageUtils.persistImage(getApplication(), it)
            }
            repository.addShoppingItem(
                ShoppingItemEntity(
                    shoppingListId = listId,
                    name = name,
                    unitPrice = unitPrice,
                    quantity = quantity.coerceAtLeast(1),
                    photoUri = photoPath
                )
            )
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { repository.deleteShoppingItem(id) }
    }
}
