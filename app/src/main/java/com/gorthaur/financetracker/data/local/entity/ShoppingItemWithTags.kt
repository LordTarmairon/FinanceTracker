package com.gorthaur.financetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ShoppingItemWithTags (
    @Embedded
    val item: ShoppingItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ShoppingItemTagCrossRef::class,
            parentColumn = "shoppingItemId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
){
}