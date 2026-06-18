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
        val categories = categoryRepository.getCategories().first()
        
        val builder = StringBuilder()
        
        // 1. Export Categories Section
        builder.append("=== Categories ===\n")
        builder.append("id,name,icon,colorKey,type,isDefault\n")
        for (cat in categories) {
            val idStr = cat.id
            val nameStr = cat.name.replace("\"", "\"\"")
            val formattedName = if (nameStr.contains(",") || nameStr.contains("\"") || nameStr.contains("\n")) {
                "\"$nameStr\""
            } else {
                nameStr
            }
            val iconStr = cat.icon
            val colorKeyStr = cat.colorKey
            val typeStr = cat.type.name
            val isDefaultStr = cat.isDefault.toString()
            builder.append("$idStr,$formattedName,$iconStr,$colorKeyStr,$typeStr,$isDefaultStr\n")
        }
        
        builder.append("\n")
        
        // 2. Export Transactions Section
        builder.append("=== Transactions ===\n")
        builder.append("date,type,categoryId,amount,note\n")
        
        val sortedTransactions = transactions.sortedByDescending { it.date }
        
        for (tx in sortedTransactions) {
            val dateStr = tx.date.toString()
            val typeStr = tx.type.name
            val categoryIdStr = tx.categoryId
            val amountStr = tx.amount.toString()
            val noteStr = tx.note?.replace("\"", "\"\"") ?: ""
            val formattedNote = if (noteStr.contains(",") || noteStr.contains("\"") || noteStr.contains("\n")) {
                "\"$noteStr\""
            } else {
                noteStr
            }
            builder.append("$dateStr,$typeStr,$categoryIdStr,$amountStr,$formattedNote\n")
        }
        
        return builder.toString()
    }
}
