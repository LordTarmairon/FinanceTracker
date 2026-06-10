package com.gorthaur.financetracker.data.repository

import com.gorthaur.financetracker.data.local.database.AppDatabase
import com.gorthaur.financetracker.data.local.entity.ServiceEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingListEntity
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Punto de acceso único a la persistencia local. Envuelve los DAOs de Room
 * para que las ViewModels no dependan directamente de la base de datos.
 */
class FinanceRepository(private val db: AppDatabase) {

    private val transactionDao = db.transactionDao()
    private val serviceDao = db.serviceDao()
    private val shoppingListDao = db.shoppingListDao()
    private val shoppingItemDao = db.shoppingItemDao()

    // --- Movimientos (gastos e ingresos) ---

    fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        transactionDao.observeByType(type)

    suspend fun addTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.delete(transaction)

    // --- Servicios recurrentes ---

    fun observeServices(): Flow<List<ServiceEntity>> = serviceDao.observeAll()

    suspend fun addService(service: ServiceEntity): Long = serviceDao.insert(service)

    suspend fun updateService(service: ServiceEntity) = serviceDao.update(service)

    suspend fun deleteService(service: ServiceEntity) = serviceDao.delete(service)

    // --- Listas de la compra ---

    fun observeShoppingLists(): Flow<List<ShoppingListEntity>> = shoppingListDao.observeAll()

    fun observeShoppingList(id: Long): Flow<ShoppingListEntity?> = shoppingListDao.observeById(id)

    suspend fun addShoppingList(list: ShoppingListEntity): Long = shoppingListDao.insert(list)

    suspend fun updateShoppingList(list: ShoppingListEntity) = shoppingListDao.update(list)

    suspend fun deleteShoppingList(id: Long) = shoppingListDao.deleteById(id)

    fun observeShoppingItems(listId: Long): Flow<List<ShoppingItemEntity>> =
        shoppingItemDao.observeByShoppingListId(listId)

    suspend fun addShoppingItem(item: ShoppingItemEntity): Long = shoppingItemDao.insert(item)

    suspend fun updateShoppingItem(item: ShoppingItemEntity) = shoppingItemDao.update(item)

    suspend fun deleteShoppingItem(id: Long) = shoppingItemDao.deleteById(id)
}
