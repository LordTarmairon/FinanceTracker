package com.gorthaur.financetracker.ui.screens.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.data.local.database.DatabaseProvider
import com.gorthaur.financetracker.data.local.entity.ServiceEntity
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class ServicesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FinanceRepository(DatabaseProvider.getDatabase(app))

    val services: StateFlow<List<ServiceEntity>> =
        repository.observeServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Total mensual de los servicios activos. */
    val monthlyTotal: StateFlow<Double> =
        repository.observeServices()
            .map { list -> list.filter { it.active }.sumOf { it.amount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun saveService(
        id: Long?,
        name: String,
        amount: Double,
        currencyCode: String,
        category: TransactionCategory,
        billingDayOfMonth: Int,
        active: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            val entity = ServiceEntity(
                id = id ?: 0,
                name = name,
                amount = amount,
                currencyCode = currencyCode,
                category = category.name,
                billingDayOfMonth = billingDayOfMonth.coerceIn(1, 31),
                active = active,
                notes = notes
            )
            if (id == null) repository.addService(entity)
            else repository.updateService(entity)
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch { repository.deleteService(service) }
    }

    /** Registra el pago de un servicio creando un gasto en la fecha de facturación de este mes. */
    fun registerPayment(service: ServiceEntity) {
        viewModelScope.launch {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val day = service.billingDayOfMonth.coerceIn(1, now.toLocalDate().lengthOfMonth())
            val date = now.withDayOfMonth(day)
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
        }
    }
}
