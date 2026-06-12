package com.gorthaur.financetracker.ui.screens.shopping

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.core.util.ImageUtils
import com.gorthaur.financetracker.data.local.SettingsDataStore
import com.gorthaur.financetracker.data.local.database.DatabaseProvider
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingListEntity
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

/** Resumen de una lista para la vista general: total, nº de ítems y progreso. */
data class ShoppingListSummary(
    val list: ShoppingListEntity,
    val itemCount: Int,
    val purchasedCount: Int,
    val total: Double
)

class ShoppingViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FinanceRepository(DatabaseProvider.getDatabase(app))
    private val settings = SettingsDataStore(app)

    val lists: StateFlow<List<ShoppingListEntity>> =
        repository.observeShoppingLists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Listas con su total (ítems + impuestos) y nº de ítems / comprados. */
    val summaries: StateFlow<List<ShoppingListSummary>> =
        combine(
            repository.observeShoppingLists(),
            repository.observeAllShoppingItems()
        ) { lists, allItems ->
            val itemsByList = allItems.groupBy { it.shoppingListId }
            lists.map { list ->
                val items = itemsByList[list.id].orEmpty()
                ShoppingListSummary(
                    list = list,
                    itemCount = items.size,
                    purchasedCount = items.count { it.purchased },
                    total = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity } + list.taxAmount
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun deleteList(list: ShoppingListEntity) {
        viewModelScope.launch {
            // Si la lista ya estaba contabilizada como gasto, lo retiramos.
            list.expenseTransactionId?.let { repository.deleteTransactionById(it) }
            repository.deleteShoppingList(list.id)
        }
    }

    /** Duplica una lista (ítems y precios) para reutilizarla, sin marcar como comprada. */
    fun copyList(list: ShoppingListEntity) {
        viewModelScope.launch {
            val newId = repository.addShoppingList(
                list.copy(
                    id = 0,
                    name = list.name + " (copia)",
                    dateEpochMillis = Instant.now().toEpochMilli(),
                    completed = false,
                    expenseTransactionId = null
                )
            )
            repository.getShoppingItems(list.id).forEach { item ->
                repository.addShoppingItem(
                    item.copy(id = 0, shoppingListId = newId, purchased = false)
                )
            }
        }
    }

    fun addItem(listId: Long, name: String, unitPrice: Double?, quantity: Int, photoSource: Uri?) {
        viewModelScope.launch {
            val photoPath = photoSource?.let { ImageUtils.persistImage(getApplication(), it) }
            repository.addShoppingItem(
                ShoppingItemEntity(
                    shoppingListId = listId,
                    name = name,
                    unitPrice = unitPrice,
                    quantity = quantity.coerceAtLeast(1),
                    photoUri = photoPath
                )
            )
            syncList(listId)
        }
    }

    fun updateItem(
        item: ShoppingItemEntity,
        name: String,
        unitPrice: Double?,
        quantity: Int,
        photoSource: Uri? = null
    ) {
        viewModelScope.launch {
            // Si se elige una foto nueva la guardamos; si no, se conserva la actual.
            val photoPath = photoSource?.let { ImageUtils.persistImage(getApplication(), it) }
                ?: item.photoUri
            repository.updateShoppingItem(
                item.copy(
                    name = name,
                    unitPrice = unitPrice,
                    quantity = quantity.coerceAtLeast(1),
                    photoUri = photoPath
                )
            )
            syncList(item.shoppingListId)
        }
    }

    fun togglePurchased(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(purchased = !item.purchased))
            syncList(item.shoppingListId)
        }
    }

    fun deleteItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item.id)
            syncList(item.shoppingListId)
        }
    }

    fun setTax(list: ShoppingListEntity, taxAmount: Double) {
        viewModelScope.launch {
            repository.updateShoppingList(list.copy(taxAmount = taxAmount))
            syncList(list.id)
        }
    }

    /**
     * Recalcula el estado de completado: si la lista tiene ítems y todos están
     * comprados, se marca completada y se registra (o actualiza) un gasto con el
     * total. Si deja de estarlo, se retira ese gasto.
     */
    private suspend fun syncList(listId: Long) {
        val list = repository.getShoppingList(listId) ?: return
        val items = repository.getShoppingItems(listId)
        val hasItems = items.isNotEmpty()
        val allPurchased = hasItems && items.all { it.purchased }
        val total = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity } + list.taxAmount

        if (allPurchased) {
            val existingId = list.expenseTransactionId
            if (existingId != null && repository.getTransaction(existingId) != null) {
                repository.updateTransaction(
                    repository.getTransaction(existingId)!!.copy(amount = total, title = list.name)
                )
                if (!list.completed) {
                    repository.updateShoppingList(list.copy(completed = true))
                }
            } else {
                val txId = repository.addTransaction(
                    TransactionEntity(
                        type = TransactionType.EXPENSE.name,
                        title = list.name,
                        amount = total,
                        currencyCode = list.currencyCode,
                        category = TransactionCategory.SHOPPING.name,
                        dateEpochMillis = Instant.now().toEpochMilli(),
                        source = "SHOPPING"
                    )
                )
                repository.updateShoppingList(list.copy(completed = true, expenseTransactionId = txId))
            }
        } else {
            list.expenseTransactionId?.let { repository.deleteTransactionById(it) }
            if (list.completed || list.expenseTransactionId != null) {
                repository.updateShoppingList(list.copy(completed = false, expenseTransactionId = null))
            }
        }
    }
}
