package com.gorthaur.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "shopping_item_tag_cross_ref",
    primaryKeys = ["shoppingItemId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingItemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shoppingItemId"), Index("tagId")]
)
data class ShoppingItemTagCrossRef(
    val shoppingItemId: Long,
    val tagId: Long
)