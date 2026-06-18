package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ExportTransactionsUseCaseTest {

    private class FakeTransactionRepository(
        var transactions: List<Transaction> = emptyList()
    ) : TransactionRepository {
        override fun getTransactions(): Flow<List<Transaction>> = flowOf(transactions)
        override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>> = flowOf(transactions)
        override suspend fun getTransactionById(id: String): Transaction? = transactions.find { it.id == id }
        override suspend fun insertTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: String) {}
        override suspend fun deleteAllTransactions() {}
    }

    private class FakeCategoryRepository(
        var categories: List<Category> = emptyList()
    ) : CategoryRepository {
        override fun getCategories(): Flow<List<Category>> = flowOf(categories)
        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun insertCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
        override suspend fun seedDefaultCategories() {}
        override suspend fun deleteAllCategories() { categories = emptyList() }
    }

    @Test
    fun testExportToCsv_exportsAndFormatsCorrectly() = runTest {
        val txRepository = FakeTransactionRepository(
            transactions = listOf(
                Transaction(
                    id = "1",
                    type = TransactionType.Expense,
                    amount = 50000,
                    categoryId = "food",
                    note = "Lunch, Starbucks",
                    date = LocalDate(2026, 6, 18),
                    createdAt = Instant.fromEpochMilliseconds(0)
                ),
                Transaction(
                    id = "2",
                    type = TransactionType.Income,
                    amount = 1000000,
                    categoryId = "salary",
                    note = "Monthly Salary \"June\"",
                    date = LocalDate(2026, 6, 19),
                    createdAt = Instant.fromEpochMilliseconds(0)
                )
            )
        )

        val catRepository = FakeCategoryRepository(
            categories = listOf(
                Category("food", "Food", "restaurant", "#FF9800", TransactionType.Expense, true),
                Category("salary", "Salary", "attach_money", "#4CAF50", TransactionType.Income, true)
            )
        )

        val exportUseCase = ExportTransactionsUseCase(txRepository, catRepository)
        val csvResult = exportUseCase()

        val expected = buildString {
            append("=== Categories ===\n")
            append("id,name,icon,colorKey,type,isDefault\n")
            append("food,Food,restaurant,#FF9800,Expense,true\n")
            append("salary,Salary,attach_money,#4CAF50,Income,true\n")
            append("\n")
            append("=== Transactions ===\n")
            append("date,type,categoryId,amount,note\n")
            append("2026-06-19,Income,salary,1000000,\"Monthly Salary \"\"June\"\"\"\n")
            append("2026-06-18,Expense,food,50000,\"Lunch, Starbucks\"\n")
        }

        assertEquals(expected, csvResult)
    }
}
