package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class MonthlySummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val balance: Long
)

class GetMonthlySummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<MonthlySummary> {
        return transactionRepository.getTransactionsByMonth(month, year).map { transactions ->
            val income = transactions.filter { it.type == TransactionType.Income }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense
            )
        }
    }
}
