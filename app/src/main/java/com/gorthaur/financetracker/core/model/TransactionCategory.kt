package com.gorthaur.financetracker.core.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.gorthaur.financetracker.R

/**
 * Categorías predefinidas para clasificar gastos e ingresos.
 * Cada una conoce su tipo, su etiqueta traducible y un color para los gráficos.
 * El sistema de IA devuelve una de estas claves al procesar un ticket.
 */
enum class TransactionCategory(
    val type: TransactionType,
    @param:StringRes val labelRes: Int,
    val color: Color
) {
    // Gastos
    GROCERIES(TransactionType.EXPENSE, R.string.category_groceries, Color(0xFF4CAF50)),
    RESTAURANT(TransactionType.EXPENSE, R.string.category_restaurant, Color(0xFFFF9800)),
    TRANSPORT(TransactionType.EXPENSE, R.string.category_transport, Color(0xFF3F51B5)),
    UTILITIES(TransactionType.EXPENSE, R.string.category_utilities, Color(0xFF009688)),
    ENTERTAINMENT(TransactionType.EXPENSE, R.string.category_entertainment, Color(0xFFE91E63)),
    HEALTH(TransactionType.EXPENSE, R.string.category_health, Color(0xFFF44336)),
    SHOPPING(TransactionType.EXPENSE, R.string.category_shopping, Color(0xFF9C27B0)),
    HOUSING(TransactionType.EXPENSE, R.string.category_housing, Color(0xFF795548)),
    OTHER_EXPENSE(TransactionType.EXPENSE, R.string.category_other_expense, Color(0xFF607D8B)),

    // Ingresos
    SALARY(TransactionType.INCOME, R.string.category_salary, Color(0xFF2E7D32)),
    FREELANCE(TransactionType.INCOME, R.string.category_freelance, Color(0xFF00897B)),
    INVESTMENT(TransactionType.INCOME, R.string.category_investment, Color(0xFF1565C0)),
    GIFT(TransactionType.INCOME, R.string.category_gift, Color(0xFFAD1457)),
    OTHER_INCOME(TransactionType.INCOME, R.string.category_other_income, Color(0xFF546E7A));

    companion object {
        fun forType(type: TransactionType): List<TransactionCategory> =
            entries.filter { it.type == type }

        fun fromKey(key: String?, type: TransactionType): TransactionCategory {
            val match = entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
            if (match != null && match.type == type) return match
            return defaultFor(type)
        }

        fun defaultFor(type: TransactionType): TransactionCategory =
            if (type == TransactionType.INCOME) OTHER_INCOME else OTHER_EXPENSE
    }
}
