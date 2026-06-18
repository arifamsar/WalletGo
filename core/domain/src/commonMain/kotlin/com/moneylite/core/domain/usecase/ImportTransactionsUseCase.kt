package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.random.Random

class ImportTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) {
    suspend operator fun invoke(csvContent: String): Result<Int> {
        return try {
            val rawLines = csvContent.lines().map { it.trim() }
            val lines = rawLines.filter { it.isNotBlank() }
            if (lines.isEmpty()) {
                return Result.failure(IllegalArgumentException("CSV file is empty"))
            }

            val categoryLines = mutableListOf<String>()
            val transactionLines = mutableListOf<String>()
            
            var currentSection = ""
            val isSectioned = lines.any { it == "=== Categories ===" }
            
            if (isSectioned) {
                for (line in lines) {
                    if (line == "=== Categories ===") {
                        currentSection = "categories"
                        continue
                    } else if (line == "=== Transactions ===") {
                        currentSection = "transactions"
                        continue
                    }
                    
                    if (currentSection == "categories") {
                        categoryLines.add(line)
                    } else if (currentSection == "transactions") {
                        transactionLines.add(line)
                    }
                }
            } else {
                // Parse as old format: treat all lines as transactions
                transactionLines.addAll(lines)
            }

            // 1. Process Categories (if present)
            if (isSectioned && categoryLines.isNotEmpty()) {
                val existingCategories = categoryRepository.getCategories().first().associateBy { it.id }
                for (line in categoryLines) {
                    val cols = parseCsvLine(line)
                    if (cols.size < 5) continue
                    val id = cols[0].trim()
                    if (id.lowercase() == "id") continue // skip header
                    
                    val name = cols[1].trim()
                    val icon = cols[2].trim()
                    val colorKey = cols[3].trim()
                    val typeStr = cols[4].trim()
                    val isDefaultStr = if (cols.size >= 6) cols[5].trim() else "false"
                    
                    val type = try {
                        TransactionType.valueOf(typeStr)
                    } catch (e: Exception) {
                        continue
                    }
                    val isDefault = isDefaultStr.lowercase().toBoolean()
                    
                    if (!existingCategories.containsKey(id)) {
                        val category = Category(
                            id = id,
                            name = name,
                            icon = icon,
                            colorKey = colorKey,
                            type = type,
                            isDefault = isDefault
                        )
                        categoryRepository.insertCategory(category)
                    }
                }
            }

            // Refetch active categories to resolve relations
            val allCategories = categoryRepository.getCategories().first()
            val categoryByIdMap = allCategories.associateBy { it.id }
            val categoryByNameMap = allCategories.associateBy { it.name.lowercase() }

            // 2. Process Transactions
            var count = 0
            val baseTime = Clock.System.now().toEpochMilliseconds()
            
            val isFirstLineHeader = transactionLines.firstOrNull()?.let {
                val cols = parseCsvLine(it)
                cols.isNotEmpty() && (cols[0].trim().lowercase() == "date" || cols[0].trim().lowercase() == "date")
            } ?: false
            
            val startIndex = if (isFirstLineHeader) 1 else 0

            for (index in startIndex until transactionLines.size) {
                val line = transactionLines[index]
                val cols = parseCsvLine(line)
                if (cols.size < 4) continue

                val dateStr = cols[0].trim()
                val typeStr = cols[1].trim()
                val categoryRef = cols[2].trim()
                val amountStr = cols[3].trim()
                val noteStr = if (cols.size >= 5) cols[4].trim() else ""

                val date = try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    continue // Skip rows with invalid date formats
                }
                
                val type = try {
                    TransactionType.valueOf(typeStr)
                } catch (e: Exception) {
                    continue
                }
                
                val amount = amountStr.toLongOrNull() ?: continue
                val note = noteStr.ifBlank { null }

                // Resolve category ID: try matching by ID first, then Name
                var categoryId = categoryByIdMap[categoryRef]?.id
                if (categoryId == null) {
                    categoryId = categoryByNameMap[categoryRef.lowercase()]?.id
                }
                if (categoryId == null) {
                    val fallback = allCategories.firstOrNull { it.id == "other" || it.name.lowercase() == "other" }
                    categoryId = fallback?.id ?: "other"
                }

                val transaction = Transaction(
                    id = "${baseTime}_${index}_${Random.nextInt(1000)}",
                    type = type,
                    amount = amount,
                    categoryId = categoryId,
                    note = note,
                    date = date,
                    createdAt = Clock.System.now()
                )

                addTransactionUseCase(transaction).getOrThrow()
                count++
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString())
                current.setLength(0)
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
