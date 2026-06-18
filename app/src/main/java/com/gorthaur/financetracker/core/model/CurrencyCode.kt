package com.gorthaur.financetracker.core.model

enum class CurrencyCode (val code: String, val symbol: String) {
    EUR("EUR", "€"),
    USD("USD", "$"),
    CAD("CAD", "$"),
    KRW("KRW", "₩");

    companion object {
        fun fromCode(code: String): CurrencyCode? {
            return entries.firstOrNull { it.code.equals(code, true) }
        }
    }
}