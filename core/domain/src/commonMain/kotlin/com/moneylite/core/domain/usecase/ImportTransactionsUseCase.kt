package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.BackupData
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class ImportTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend operator fun invoke(jsonContent: String): Result<Int> {
        return try {
            val cleanContent = if (jsonContent.startsWith("\uFEFF")) {
                jsonContent.substring(1)
            } else {
                jsonContent
            }
            
            val backupData = json.decodeFromString<BackupData>(cleanContent)

            val existingCategories = categoryRepository.getCategories().first().associateBy { it.id }.toMutableMap()

            // 1. Process Categories
            for (category in backupData.categories) {
                if (!existingCategories.containsKey(category.id)) {
                    categoryRepository.insertCategory(category)
                    existingCategories[category.id] = category
                }
            }

            // 2. Process Transactions
            var count = 0
            for (transaction in backupData.transactions) {
                transactionRepository.insertTransaction(transaction)
                count++
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
