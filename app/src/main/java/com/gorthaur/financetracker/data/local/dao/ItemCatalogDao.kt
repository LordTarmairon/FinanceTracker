package com.gorthaur.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gorthaur.financetracker.data.local.entity.ItemCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCatalogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ItemCatalogEntity): Long

    @Query("SELECT * FROM item_catalog ORDER BY name ASC")
    fun observeAll(): Flow<List<ItemCatalogEntity>>

    @Query("SELECT * FROM item_catalog WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): ItemCatalogEntity?

    @Query("SELECT * FROM item_catalog  WHERE name LIKE'%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<ItemCatalogEntity>>
}