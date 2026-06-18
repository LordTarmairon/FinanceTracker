package com.gorthaur.financetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gorthaur.financetracker.data.local.dao.ItemCatalogDao
import com.gorthaur.financetracker.data.local.dao.ServiceDao
import com.gorthaur.financetracker.data.local.dao.ShoppingItemDao
import com.gorthaur.financetracker.data.local.dao.ShoppingListDao
import com.gorthaur.financetracker.data.local.dao.TagDao
import com.gorthaur.financetracker.data.local.dao.TransactionDao
import com.gorthaur.financetracker.data.local.entity.ItemCatalogEntity
import com.gorthaur.financetracker.data.local.entity.ServiceEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingItemTagCrossRef
import com.gorthaur.financetracker.data.local.entity.ShoppingListEntity
import com.gorthaur.financetracker.data.local.entity.TagEntity
import com.gorthaur.financetracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        ItemCatalogEntity::class,
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        TagEntity::class,
        ShoppingItemTagCrossRef::class,
        TransactionEntity::class,
        ServiceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemCatalogDao(): ItemCatalogDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun tagDao(): TagDao
    abstract fun transactionDao(): TransactionDao
    abstract fun serviceDao(): ServiceDao
}