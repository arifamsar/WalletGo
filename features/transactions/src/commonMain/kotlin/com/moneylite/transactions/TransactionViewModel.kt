package com.moneylite.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.TransactionUiModel
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow<TransactionType?>(null)

    private val _uiState = MutableStateFlow(TransactionListState())
    val uiState: StateFlow<TransactionListState> = _uiState

    private val _effects = MutableSharedFlow<TransactionListEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<TransactionListEffect> = _effects

    private var loadJob: Job? = null
    private val pendingUndoTransactions = mutableMapOf<String, Transaction>()

    init {
        loadTransactions()
    }

    fun onIntent(intent: TransactionListIntent) {
        when (intent) {
            is TransactionListIntent.SearchQueryChanged -> {
                _searchQuery.value = intent.query
            }
            is TransactionListIntent.TypeFilterSelected -> {
                _selectedType.value = intent.type
            }
            is TransactionListIntent.RequestDeleteTransaction -> {
                _uiState.value = _uiState.value.copy(deleteCandidate = intent.transaction)
            }
            is TransactionListIntent.DismissDeleteDialog -> {
                _uiState.value = _uiState.value.copy(deleteCandidate = null)
            }
            is TransactionListIntent.ConfirmDeleteTransaction -> {
                confirmDeleteTransaction()
            }
            is TransactionListIntent.UndoDeleteTransaction -> {
                undoDeleteTransaction(intent.id)
            }
            is TransactionListIntent.ClearPendingUndo -> {
                pendingUndoTransactions.remove(intent.id)
            }
            is TransactionListIntent.Load -> {
                loadTransactions()
            }
        }
    }

    private fun confirmDeleteTransaction() {
        val candidate = _uiState.value.deleteCandidate ?: return
        _uiState.value = _uiState.value.copy(deleteCandidate = null)

        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(candidate.id) ?: return@launch
            transactionRepository.deleteTransaction(candidate.id)
            pendingUndoTransactions[candidate.id] = transaction
            _effects.emit(TransactionListEffect.TransactionDeleted(candidate.id))
        }
    }

    private fun undoDeleteTransaction(id: String) {
        val transaction = pendingUndoTransactions.remove(id) ?: return

        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    private fun loadTransactions() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            combine(
                getTransactionsUseCase(),
                categoryRepository.getCategories(),
                _searchQuery,
                _selectedType
            ) { transactions, categories, query, type ->
                val categoryMap = categories.associateBy { it.id }
                val trimmedQuery = query.trim()
                val uiModels = transactions.map { tx ->
                    val cat = categoryMap[tx.categoryId]
                    TransactionUiModel(
                        id = tx.id,
                        type = tx.type,
                        amount = tx.amount,
                        note = tx.note,
                        date = tx.date,
                        categoryId = tx.categoryId,
                        categoryName = cat?.name ?: "Other",
                        categoryIcon = cat?.icon ?: "more_horiz",
                        categoryColor = cat?.colorKey ?: "#607D8B"
                    )
                }.filter { transaction ->
                    val matchesQuery = trimmedQuery.isEmpty() ||
                        transaction.note?.contains(trimmedQuery, ignoreCase = true) == true ||
                        transaction.categoryName.contains(trimmedQuery, ignoreCase = true)
                    val matchesType = type == null || transaction.type == type
                    matchesQuery && matchesType
                }

                val grouped = uiModels.groupBy { it.date }

                TransactionListState(
                    isLoading = false,
                    searchQuery = query,
                    selectedType = type,
                    transactions = uiModels,
                    groupedTransactions = grouped,
                    deleteCandidate = _uiState.value.deleteCandidate
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
