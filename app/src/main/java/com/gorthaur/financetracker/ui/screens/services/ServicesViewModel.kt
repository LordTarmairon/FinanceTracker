package com.gorthaur.financetracker.ui.screens.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.data.local.SettingsDataStore
import com.gorthaur.financetracker.data.local.database.DatabaseProvider
import com.gorthaur.financetracker.data.local.entity.ServiceEntity
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

data class ServicesUiState(
    val monthlyTotal: Double = 0.0,
    val currency: CurrencyCode = CurrencyCode.EUR
)

class ServicesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FinanceRepository(DatabaseProvider.getDatabase(app))
    private val settings = SettingsDataStore(app)

    val services: StateFlow<List<ServiceEntity>> =
        repository.observeServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Total mensual de los servicios activos, convertido a la moneda configurada. */
    val uiState: StateFlow<ServicesUiState> =
        combine(repository.observeServices(), settings.preferencesFlow) { list, prefs ->
            val total = list.filter { it.active }.sumOf {
                prefs.exchangeRates.convert(it.amount, it.currencyCode, prefs.defaultCurrency)
            }
            ServicesUiState(total, prefs.defaultCurrency)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServicesUiState())

    init {
        processRecurringCharges()
    }

    fun saveService(
        id: Long?,
        name: String,
        amount: Double,
        currencyCode: String,
        category: TransactionCategory,
        billingDayOfMonth: Int,
        active: Boolean,
        monthly: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            val existing = if (id != null) repository.getServices().firstOrNull { it.id == id } else null
            val entity = ServiceEntity(
                id = id ?: 0,
                name = name,
                amount = amount,
                currencyCode = currencyCode,
                category = category.name,
                billingDayOfMonth = billingDayOfMonth.coerceIn(1, 31),
                active = active,
                monthly = monthly,
                lastChargedEpochMillis = existing?.lastChargedEpochMillis,
                notes = notes
            )
            if (id == null) repository.addService(entity)
            else repository.updateService(entity)
            processRecurringCharges()
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch { repository.deleteService(service) }
    }

    /** Registra el pago de un servicio creando un gasto en la fecha de facturación de este mes. */
    fun registerPayment(service: ServiceEntity) {
        viewModelScope.launch {
            chargeService(service, ZonedDateTime.now(ZoneId.systemDefault()))
        }
    }

    /**
     * Genera automáticamente el cobro de los servicios mensuales activos cuyo día
     * de facturación de este mes ya ha llegado y que aún no se han cobrado este mes.
     * Se ejecuta al abrir la pantalla, de modo que cada mes vuelve a registrarse.
     */
    private fun processRecurringCharges() {
        viewModelScope.launch {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val currentMonth = YearMonth.from(now)
            repository.getServices()
                .filter { it.active && it.monthly }
                .forEach { service ->
                    val billingDay = service.billingDayOfMonth.coerceIn(1, now.toLocalDate().lengthOfMonth())
                    val alreadyChargedThisMonth = service.lastChargedEpochMillis?.let {
                        YearMonth.from(java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())) == currentMonth
                    } ?: false
                    if (!alreadyChargedThisMonth && now.dayOfMonth >= billingDay) {
                        chargeService(service, now.withDayOfMonth(billingDay))
                    }
                }
        }
    }

    private suspend fun chargeService(service: ServiceEntity, date: ZonedDateTime) {
        repository.addTransaction(
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                title = service.name,
                amount = service.amount,
                currencyCode = service.currencyCode,
                category = TransactionCategory.fromKey(service.category, TransactionType.EXPENSE).name,
                dateEpochMillis = date.toInstant().toEpochMilli(),
                notes = service.notes,
                source = "SERVICE"
            )
        )
        repository.updateService(service.copy(lastChargedEpochMillis = date.toInstant().toEpochMilli()))
    }
}
