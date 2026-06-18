package com.gorthaur.financetracker.core.util

import com.gorthaur.financetracker.core.model.CurrencyCode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formateadores compartidos para importes y fechas. */
object Formatters {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    private val monthFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())

    fun money(amount: Double, currency: CurrencyCode): String {
        val formatted = String.format(Locale.getDefault(), "%,.2f", amount)
        return "$formatted ${currency.symbol}"
    }

    fun money(amount: Double, currencyCode: String): String {
        val currency = CurrencyCode.fromCode(currencyCode) ?: CurrencyCode.EUR
        return money(amount, currency)
    }

    fun date(epochMillis: Long): String =
        dateFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

    fun month(epochMillis: Long): String =
        monthFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
            .replaceFirstChar { it.uppercase() }
}
