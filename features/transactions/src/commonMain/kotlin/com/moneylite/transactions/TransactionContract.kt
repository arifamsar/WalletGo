package com.moneylite.transactions

import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.TransactionUiModel
import kotlinx.datetime.LocalDate

data class TransactionListState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedType: TransactionType? = null, // null means "All"
    val transactions: List<TransactionUiModel> = emptyList(),
    val groupedTransactions: Map<LocalDate, List<TransactionUiModel>> = emptyMap(),
    val deleteCandidate: TransactionUiModel? = null
)

sealed interface TransactionListIntent {
    data class SearchQueryChanged(val query: String) : TransactionListIntent
    data class TypeFilterSelected(val type: TransactionType?) : TransactionListIntent
    data class RequestDeleteTransaction(val transaction: TransactionUiModel) : TransactionListIntent
    data object DismissDeleteDialog : TransactionListIntent
    data object ConfirmDeleteTransaction : TransactionListIntent
    data class UndoDeleteTransaction(val id: String) : TransactionListIntent
    data class ClearPendingUndo(val id: String) : TransactionListIntent
    data object Load : TransactionListIntent
}

sealed interface TransactionListEffect {
    data class TransactionDeleted(val id: String) : TransactionListEffect
}
