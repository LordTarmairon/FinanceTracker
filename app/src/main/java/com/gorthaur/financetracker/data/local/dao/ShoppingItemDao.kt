package com.gorthaur.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gorthaur.financetracker.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Insert
    suspend fun insert(item: ShoppingItemEntity): Long

    @Query("SELECT * FROM shopping_items WHERE shoppingListId = :shoppingListId ORDER BY id ASC")
    fun observeByShoppingListId(shoppingListId: Long): Flow<List<ShoppingItemEntity>>

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shopping_items WHERE shoppingListId = :shoppingListId")
    suspend fun deleteAllFromShoppingList(shoppingListId: Long)
}