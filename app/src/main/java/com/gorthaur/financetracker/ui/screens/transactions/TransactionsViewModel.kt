package com.gorthaur.financetracker.ui.screens.transactions

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.ReceiptScan
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import com.gorthaur.financetracker.core.util.ImageUtils
import com.gorthaur.financetracker.data.local.SettingsDataStore
import com.gorthaur.financetracker.data.local.database.DatabaseProvider
import com.gorthaur.financetracker.data.local.entity.TransactionEntity
import com.gorthaur.financetracker.data.remote.ReceiptAiClient
import com.gorthaur.financetracker.data.remote.ScanErrorReason
import com.gorthaur.financetracker.data.remote.ScanResult
import com.gorthaur.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/** Estado del proceso de escaneo de un ticket con IA. */
sealed interface ScanState {
    data object Idle : ScanState
    data object Loading : ScanState
    data class Success(val scan: ReceiptScan, val photoPath: String?) : ScanState
    data class Error(val reason: ScanErrorReason, val detail: String?) : ScanState
}

class TransactionsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FinanceRepository(DatabaseProvider.getDatabase(app))
    private val settings = SettingsDataStore(app)
    private val aiClient = ReceiptAiClient()

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    val incomes: StateFlow<List<TransactionEntity>> =
        repository.observeTransactionsByType(TransactionType.INCOME.name)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<TransactionEntity>> =
        repository.observeTransactionsByType(TransactionType.EXPENSE.name)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val defaultCurrency: StateFlow<CurrencyCode> =
        settings.preferencesFlow.map { it.defaultCurrency }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CurrencyCode.EUR)

    /** Lanza el escaneo IA de la foto indicada (cámara o galería). */
    fun scanReceipt(source: Uri) {
        viewModelScope.launch {
            _scanState.value = ScanState.Loading
            val context = getApplication<Application>()

            val photoPath = ImageUtils.persistImage(context, source)
            if (photoPath == null) {
                _scanState.value = ScanState.Error(ScanErrorReason.NO_IMAGE, null)
                return@launch
            }
            val base64 = ImageUtils.toBase64Jpeg(context, File(photoPath).toUri())
            if (base64 == null) {
                _scanState.value = ScanState.Error(ScanErrorReason.NO_IMAGE, null)
                return@launch
            }

            val prefs = settings.preferencesFlow.first()
            when (val result = aiClient.scan(base64, prefs.aiApiKey, prefs.defaultCurrency)) {
                is ScanResult.Success ->
                    _scanState.value = ScanState.Success(result.scan, photoPath)
                is ScanResult.Failure ->
                    _scanState.value = ScanState.Error(result.reason, result.detail)
            }
        }
    }

    fun consumeScan() {
        _scanState.value = ScanState.Idle
    }

    fun saveTransaction(
        id: Long?,
        type: TransactionType,
        title: String,
        amount: Double,
        currencyCode: String,
        category: TransactionCategory,
        dateEpochMillis: Long,
        notes: String?,
        photoPath: String?,
        source: String = "MANUAL"
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id ?: 0,
                type = type.name,
                title = title,
                amount = amount,
                currencyCode = currencyCode,
                category = category.name,
                dateEpochMillis = dateEpochMillis,
                notes = notes,
                photoUri = photoPath,
                source = source
            )
            if (id == null) repository.addTransaction(entity)
            else repository.updateTransaction(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
