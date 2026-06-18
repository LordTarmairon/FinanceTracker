package com.gorthaur.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gorthaur.financetracker.data.local.entity.ShoppingItemTagCrossRef
import com.gorthaur.financetracker.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: ShoppingItemTagCrossRef)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN shopping_item_tag_cross_ref
        ON tags.id = shopping_item_tag_cross_ref.tagId
        WHERE shopping_item_tag_cross_ref.shoppingItemId = :shoppingItemId
        ORDER BY tags.name ASC
    """)
    fun observeTagsForShoppingItem(shoppingItemId: Long): Flow<List<TagEntity>>

    @Query("DELETE FROM shopping_item_tag_cross_ref WHERE shoppingItemId = :shoppingItemId")
    suspend fun deleteCrossRefsForShoppingItem(shoppingItemId: Long)
}