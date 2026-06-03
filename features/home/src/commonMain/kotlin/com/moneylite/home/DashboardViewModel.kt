package com.moneylite.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.usecase.GetBudgetProgressUseCase
import com.moneylite.core.domain.usecase.GetMonthlySummaryUseCase
import com.moneylite.core.domain.usecase.GetTransactionsUseCase
import com.moneylite.core.domain.model.TransactionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardViewModel(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getBudgetProgressUseCase: GetBudgetProgressUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState

    init {
        loadDashboardData()
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.Load -> loadDashboardData()
            is DashboardIntent.DeleteTransaction -> deleteTransaction(intent.id)
        }
    }

    private fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }

    private fun loadDashboardData() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val month = now.month.ordinal + 1
        val year = now.year

        viewModelScope.launch {
            combine(
                getMonthlySummaryUseCase(month, year),
                getBudgetProgressUseCase(month, year),
                getTransactionsUseCase(),
                categoryRepository.getCategories()
            ) { summary, budgetProgress, transactions, categories ->
                val categoryMap = categories.associateBy { it.id }

                // Filter transactions of this month only and take recent 5
                val thisMonthTransactions = transactions.filter {
                    it.date.month.ordinal + 1 == month && it.date.year == year
                }

                val recentUiModels = thisMonthTransactions.take(5).map { tx ->
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
                }

                val monthlyUiModels = thisMonthTransactions.map { tx ->
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
                }

                DashboardState(
                    isLoading = false,
                    monthBalance = summary.balance,
                    monthIncome = summary.totalIncome,
                    monthExpense = summary.totalExpense,
                    budgetLimit = budgetProgress.limitAmount,
                    budgetUsedPercent = budgetProgress.progress,
                    recentTransactions = recentUiModels,
                    monthlyTransactions = monthlyUiModels
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
