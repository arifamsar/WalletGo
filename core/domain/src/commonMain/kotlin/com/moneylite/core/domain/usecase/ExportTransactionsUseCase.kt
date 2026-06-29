package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.BackupData
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ExportTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend operator fun invoke(): String {
        val transactions = transactionRepository.getTransactions().first()
        val categories = categoryRepository.getCategories().first()
        
        val backupData = BackupData(
            categories = categories,
            transactions = transactions
        )
        
        return json.encodeToString(backupData)
    }
}
