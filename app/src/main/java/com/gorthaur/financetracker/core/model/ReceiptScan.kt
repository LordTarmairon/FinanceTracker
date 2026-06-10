package com.gorthaur.financetracker.core.model

/**
 * Datos extraídos por la IA a partir de la foto de un ticket o factura.
 * Se usan para prerellenar el formulario de un nuevo movimiento.
 */
data class ReceiptScan(
    val type: TransactionType,
    val title: String,
    val amount: Double,
    val currencyCode: String,
    val category: TransactionCategory,
    val dateEpochMillis: Long,
    val notes: String?
)
