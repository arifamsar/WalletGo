package com.moneylite.home

import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.TransactionUiModel
import kotlinx.datetime.LocalDate

data class DashboardState(
    val isLoading: Boolean = true,
    val monthBalance: Long = 0L,
    val monthIncome: Long = 0L,
    val monthExpense: Long = 0L,
    val budgetLimit: Long = 0L,
    val budgetUsedPercent: Float = 0f,
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val monthlyTransactions: List<TransactionUiModel> = emptyList()
)

sealed interface DashboardIntent {
    data object Load : DashboardIntent
    data class DeleteTransaction(val id: String) : DashboardIntent
}

sealed interface DashboardEffect {
    data object NavigateToAddTransaction : DashboardEffect
    data object NavigateToTransactions : DashboardEffect
}
