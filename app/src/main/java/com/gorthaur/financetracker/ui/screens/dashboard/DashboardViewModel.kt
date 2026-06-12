package com.gorthaur.financetracker.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.ExchangeRates
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.data.local.SettingsDataStore
import com.gorthaur.financetracker.data.local.database.DatabaseProvider
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId

/** Filtro de la lista de movimientos recientes del panel. */
enum class DashboardFilter { ALL, EXPENSE, INCOME }

data class CategorySlice(
    val category: TransactionCategory,
    val amount: Double,
    val fraction: Float
)

data class MonthBar(
    val label: String,
    val income: Double,
    val expense: Double
)

data class DashboardUiState(
    val monthLabel: String = "",
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val balance: Double = 0.0,
    val currency: CurrencyCode = CurrencyCode.EUR,
    val expenseSlices: List<CategorySlice> = emptyList(),
    val monthlyBars: List<MonthBar> = emptyList(),
    val recent: List<TransactionEntity> = emptyList(),
    val filter: DashboardFilter = DashboardFilter.ALL,
    val hasData: Boolean = false
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FinanceRepository(DatabaseProvider.getDatabase(app))
    private val settings = SettingsDataStore(app)
    private val filter = MutableStateFlow(DashboardFilter.ALL)

    fun setFilter(value: DashboardFilter) {
        filter.value = value
    }

    val uiState: StateFlow<DashboardUiState> =
        combine(
            repository.observeTransactions(),
            settings.preferencesFlow,
            filter
        ) { transactions, prefs, activeFilter ->
            buildState(transactions, prefs.defaultCurrency, prefs.exchangeRates, activeFilter)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    private fun buildState(
        transactions: List<TransactionEntity>,
        currency: CurrencyCode,
        rates: ExchangeRates,
        activeFilter: DashboardFilter
    ): DashboardUiState {
        val zone = ZoneId.systemDefault()
        val now = YearMonth.now()

        fun monthOf(t: TransactionEntity): YearMonth =
            java.time.Instant.ofEpochMilli(t.dateEpochMillis).atZone(zone).let {
                YearMonth.of(it.year, it.month)
            }

        // Convierte el importe de cada movimiento a la moneda configurada.
        fun amountIn(t: TransactionEntity): Double =
            rates.convert(t.amount, t.currencyCode, currency)

        val thisMonth = transactions.filter { monthOf(it) == now }
        val income = thisMonth.filter { it.type == TransactionType.INCOME.name }.sumOf { amountIn(it) }
        val expense = thisMonth.filter { it.type == TransactionType.EXPENSE.name }.sumOf { amountIn(it) }

        // Reparto de gastos del mes por categoría (para el gráfico de tarta).
        val expenseByCategory = thisMonth
            .filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { TransactionCategory.fromKey(it.category, TransactionType.EXPENSE) }
            .mapValues { entry -> entry.value.sumOf { amountIn(it) } }
            .filterValues { it > 0 }
        val totalExpense = expenseByCategory.values.sum()
        val slices = expenseByCategory.entries
            .sortedByDescending { it.value }
            .map { (category, amount) ->
                CategorySlice(
                    category = category,
                    amount = amount,
                    fraction = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                )
            }

        // Movimientos recientes según el filtro activo.
        val recent = transactions
            .filter {
                when (activeFilter) {
                    DashboardFilter.ALL -> true
                    DashboardFilter.EXPENSE -> it.type == TransactionType.EXPENSE.name
                    DashboardFilter.INCOME -> it.type == TransactionType.INCOME.name
                }
            }
            .take(20)

        // Barras de los últimos 6 meses (ingresos vs gastos).
        val bars = (5 downTo 0).map { offset ->
            val month = now.minusMonths(offset.toLong())
            val ofMonth = transactions.filter { monthOf(it) == month }
            MonthBar(
                label = month.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                income = ofMonth.filter { it.type == TransactionType.INCOME.name }.sumOf { amountIn(it) },
                expense = ofMonth.filter { it.type == TransactionType.EXPENSE.name }.sumOf { amountIn(it) }
            )
        }

        return DashboardUiState(
            monthLabel = now.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + now.year,
            monthIncome = income,
            monthExpense = expense,
            balance = income - expense,
            currency = currency,
            expenseSlices = slices,
            monthlyBars = bars,
            recent = recent,
            filter = activeFilter,
            hasData = transactions.isNotEmpty()
        )
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
