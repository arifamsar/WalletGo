package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportTransactionsUseCaseTest {

    private class FakeTransactionRepository : TransactionRepository {
        val insertedTransactions = mutableListOf<Transaction>()
        override fun getTransactions(): Flow<List<Transaction>> = flowOf(insertedTransactions)
        override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>> = flowOf(insertedTransactions)
        override suspend fun getTransactionById(id: String): Transaction? = null
        override suspend fun insertTransaction(transaction: Transaction) {
            insertedTransactions.add(transaction)
        }
        override suspend fun deleteTransaction(id: String) {}
        override suspend fun deleteAllTransactions() {}
    }

    private class FakeCategoryRepository : CategoryRepository {
        var categories = listOf<Category>()
        override fun getCategories(): Flow<List<Category>> = flowOf(categories)
        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun insertCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
        override suspend fun seedDefaultCategories() {}
    }

    @Test
    fun testImportCsv_insertsTransactionsSuccessfully() = runTest {
        val txRepository = FakeTransactionRepository()
        val catRepository = FakeCategoryRepository().apply {
            categories = listOf(
                Category("food", "Food", "restaurant", "#FF9800", TransactionType.Expense, true),
                Category("salary", "Salary", "attach_money", "#4CAF50", TransactionType.Income, true)
            )
        }
        val addUseCase = AddTransactionUseCase(txRepository)
        val importUseCase = ImportTransactionsUseCase(txRepository, catRepository, addUseCase)

        val csvData = "Date,Type,Category,Amount,Note\n" +
                "2026-06-19,Income,Salary,1000000,\"Monthly Salary \"\"June\"\"\"\n" +
                "2026-06-18,Expense,Food,50000,\"Lunch, Starbucks\""

        val result = importUseCase(csvData)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow())
        assertEquals(2, txRepository.insertedTransactions.size)

        val tx1 = txRepository.insertedTransactions[0]
        assertEquals(LocalDate(2026, 6, 19), tx1.date)
        assertEquals(TransactionType.Income, tx1.type)
        assertEquals("salary", tx1.categoryId)
        assertEquals(1000000L, tx1.amount)
        assertEquals("Monthly Salary \"June\"", tx1.note)

        val tx2 = txRepository.insertedTransactions[1]
        assertEquals(LocalDate(2026, 6, 18), tx2.date)
        assertEquals(TransactionType.Expense, tx2.type)
        assertEquals("food", tx2.categoryId)
        assertEquals(50000L, tx2.amount)
        assertEquals("Lunch, Starbucks", tx2.note)
    }

    @Test
    fun testImportCsv_invalidHeader_returnsFailure() = runTest {
        val txRepository = FakeTransactionRepository()
        val catRepository = FakeCategoryRepository()
        val addUseCase = AddTransactionUseCase(txRepository)
        val importUseCase = ImportTransactionsUseCase(txRepository, catRepository, addUseCase)

        val csvData = "InvalidHeader,Type,Category,Amount\n" +
                "2026-06-18,Expense,Food,50000"

        val result = importUseCase(csvData)
        assertTrue(result.isFailure)
        assertTrue(txRepository.insertedTransactions.isEmpty())
    }
}
