package com.gorthaur.financetracker.core.model

enum class TransactionType {
    EXPENSE,
    INCOME;

    companion object {
        fun fromName(value: String?): TransactionType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: EXPENSE
    }
}
