package com.gorthaur.financetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gorthaur.financetracker.data.local.dao.ItemCatalogDao
import com.gorthaur.financetracker.data.local.dao.ShoppingItemDao
import com.gorthaur.financetracker.data.local.dao.ShoppingListDao
import com.gorthaur.financetracker.data.local.entity.ItemCatalogEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import com.gorthaur.financetracker.data.local.entity.ShoppingItemTagCrossRef
import com.gorthaur.financetracker.data.local.entity.ShoppingListEntity
import com.gorthaur.financetracker.data.local.entity.TagEntity

@Database(
    entities = [
        ItemCatalogEntity::class,
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        TagEntity::class,
        ShoppingItemTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemCatalogDao(): ItemCatalogDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
}