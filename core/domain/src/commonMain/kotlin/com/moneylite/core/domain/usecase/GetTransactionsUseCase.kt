package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        query: String = "",
        type: TransactionType? = null,
        categoryId: String? = null
    ): Flow<List<Transaction>> {
        return transactionRepository.getTransactions().map { transactions ->
            transactions
                .filter { transaction ->
                    val matchesQuery = query.isEmpty() || transaction.note?.contains(query, ignoreCase = true) == true
                    val matchesType = type == null || transaction.type == type
                    val matchesCategory = categoryId == null || transaction.categoryId == categoryId
                    matchesQuery && matchesType && matchesCategory
                }
                .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.createdAt })
        }
    }
}
