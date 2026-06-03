package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.BudgetRepository
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BudgetProgress(
    val limitAmount: Long,
    val usedAmount: Long,
    val progress: Float
)

class GetBudgetProgressUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<BudgetProgress> {
        return combine(
            budgetRepository.getBudget(month, year),
            transactionRepository.getTransactionsByMonth(month, year)
        ) { budget, transactions ->
            val limit = budget?.limitAmount ?: 0L
            val used = transactions
                .filter { it.type == TransactionType.Expense }
                .sumOf { it.amount }
            val progress = if (limit > 0) (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
            BudgetProgress(
                limitAmount = limit,
                usedAmount = used,
                progress = progress
            )
        }
    }
}
