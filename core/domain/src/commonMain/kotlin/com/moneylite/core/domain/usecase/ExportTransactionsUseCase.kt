package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first

class ExportTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): String {
        val transactions = transactionRepository.getTransactions().first()
        val categories = categoryRepository.getCategories().first().associateBy { it.id }

        val builder = StringBuilder()
        builder.append("Date,Type,Category,Amount,Note\n")

        val sortedTransactions = transactions.sortedByDescending { it.date }

        for (tx in sortedTransactions) {
            val dateStr = tx.date.toString()
            val typeStr = tx.type.name
            val categoryName = categories[tx.categoryId]?.name ?: "Other"
            val amountStr = tx.amount.toString()
            val noteStr = tx.note?.replace("\"", "\"\"") ?: ""
            val formattedNote = if (noteStr.contains(",") || noteStr.contains("\"") || noteStr.contains("\n")) {
                "\"$noteStr\""
            } else {
                noteStr
            }
            builder.append("$dateStr,$typeStr,$categoryName,$amountStr,$formattedNote\n")
        }

        return builder.toString()
    }
}
