package com.gorthaur.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un servicio recurrente (teléfono, internet, Netflix, alquiler...).
 * Registrar su pago genera un [TransactionEntity] de tipo gasto.
 */
@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val currencyCode: String,
    /** Clave de [com.gorthaur.financetracker.core.model.TransactionCategory]. */
    val category: String,
    /** Día del mes (1-31) en que se factura. */
    val billingDayOfMonth: Int,
    val active: Boolean = true,
    val notes: String? = null
)
