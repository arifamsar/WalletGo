package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
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
            val lines = csvContent.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) {
                return Result.failure(IllegalArgumentException("CSV file is empty"))
            }

            val header = lines.first()
            val headerCols = parseCsvLine(header)
            if (headerCols.size < 4 || headerCols[0].trim().lowercase() != "date" || headerCols[1].trim().lowercase() != "type") {
                return Result.failure(IllegalArgumentException("Invalid CSV header format"))
            }

            val categories = categoryRepository.getCategories().first()
            val categoryMap = categories.associateBy { it.name.lowercase() }

            var count = 0
            val baseTime = Clock.System.now().toEpochMilliseconds()

            for (index in 1 until lines.size) {
                val line = lines[index]
                val cols = parseCsvLine(line)
                if (cols.size < 4) continue

                val dateStr = cols[0].trim()
                val typeStr = cols[1].trim()
                val categoryName = cols[2].trim()
                val amountStr = cols[3].trim()
                val noteStr = if (cols.size >= 5) cols[4].trim() else ""

                val date = LocalDate.parse(dateStr)
                val type = TransactionType.valueOf(typeStr)
                val amount = amountStr.toLong()
                val note = noteStr.ifBlank { null }

                var categoryId = categoryMap[categoryName.lowercase()]?.id
                if (categoryId == null) {
                    val fallback = categories.firstOrNull { it.id == "other" || it.name.lowercase() == "other" }
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
