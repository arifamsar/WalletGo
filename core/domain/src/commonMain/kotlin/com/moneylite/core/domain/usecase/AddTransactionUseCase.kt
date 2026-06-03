package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.repository.TransactionRepository

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        if (transaction.amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }
        return try {
            transactionRepository.insertTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
