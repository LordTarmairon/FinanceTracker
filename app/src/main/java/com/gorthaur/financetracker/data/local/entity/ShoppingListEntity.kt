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
    val notes: String? = null,
    /** Impuestos pagados que no van incluidos en el precio de los artículos. */
    val taxAmount: Double = 0.0,
    /** La lista se marca completada cuando todos sus ítems están comprados. */
    val completed: Boolean = false,
    /** Id del gasto generado al completar la lista (para mantenerlo en sincronía). */
    val expenseTransactionId: Long? = null
)