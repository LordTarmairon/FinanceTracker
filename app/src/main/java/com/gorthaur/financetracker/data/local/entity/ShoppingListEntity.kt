package com.gorthaur.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val dateEpochMillis: Long,
    val currencyCode: String,
    val manualSubtotal: Double? = null,
    val tax1: Double = 0.0,
    val tax2: Double = 0.0,
    val notes: String? = null
)