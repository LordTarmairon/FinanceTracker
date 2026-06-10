package com.gorthaur.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un movimiento económico: un gasto o un ingreso.
 * Es la unidad central sobre la que el panel construye sus estadísticas.
 * Puede crearse manualmente, a partir de un servicio recurrente o desde
 * la IA que procesa una foto de un ticket ([source] = "AI_RECEIPT").
 */
@Entity(
    tableName = "transactions",
    indices = [Index("dateEpochMillis"), Index("type")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** "EXPENSE" o "INCOME" (ver [com.gorthaur.financetracker.core.model.TransactionType]). */
    val type: String,
    val title: String,
    val amount: Double,
    val currencyCode: String,
    /** Clave de [com.gorthaur.financetracker.core.model.TransactionCategory]. */
    val category: String,
    val dateEpochMillis: Long,
    val notes: String? = null,
    val photoUri: String? = null,
    /** Origen del movimiento: "MANUAL", "AI_RECEIPT" o "SERVICE". */
    val source: String = "MANUAL"
)
