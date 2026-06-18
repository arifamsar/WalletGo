package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private class FakeCategoryRepository(
        initialCategories: List<Category> = emptyList()
    ) : CategoryRepository {
        private val _categories = MutableStateFlow(initialCategories)
        override fun getCategories(): Flow<List<Category>> = _categories
        override suspend fun getCategoryById(id: String): Category? = _categories.value.find { it.id == id }
        override suspend fun insertCategory(category: Category) {
            _categories.value = _categories.value + category
        }
        override suspend fun deleteCategory(id: String) {
            _categories.value = _categories.value.filter { it.id != id }
        }
        override suspend fun seedDefaultCategories() {}
        override suspend fun deleteAllCategories() {
            _categories.value = emptyList()
        }
    }

    @Test
    fun testImportCsv_insertsTransactionsSuccessfully_backwardCompatible() = runTest {
        val txRepository = FakeTransactionRepository()
        val catRepository = FakeCategoryRepository(
            listOf(
                Category("food", "Food", "restaurant", "#FF9800", TransactionType.Expense, true),
                Category("salary", "Salary", "attach_money", "#4CAF50", TransactionType.Income, true)
            )
        )
        val addUseCase = AddTransactionUseCase(txRepository)
        val importUseCase = ImportTransactionsUseCase(txRepository, catRepository, addUseCase)

        // Old format (just transactions, matching category by Name)
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
    fun testImportCsv_sectionedFormat_importsCategoriesAndTransactions() = runTest {
        val txRepository = FakeTransactionRepository()
        val catRepository = FakeCategoryRepository(
            listOf(
                Category("food", "Food", "restaurant", "#FF9800", TransactionType.Expense, true)
            )
        )
        val addUseCase = AddTransactionUseCase(txRepository)
        val importUseCase = ImportTransactionsUseCase(txRepository, catRepository, addUseCase)

        // Sectioned format: includes custom categories and maps transactions to custom IDs
        val csvData = buildString {
            append("=== Categories ===\n")
            append("id,name,icon,colorKey,type,isDefault\n")
            append("food,Food,restaurant,#FF9800,Expense,true\n")
            append("custom1,My Custom,star,#123456,Expense,false\n")
            append("\n")
            append("=== Transactions ===\n")
            append("date,type,categoryId,amount,note\n")
            append("2026-06-19,Expense,custom1,12000,Gift\n")
            append("2026-06-18,Expense,food,50000,\"Lunch\"\n")
        }

        val result = importUseCase(csvData)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow())
        
        // Verify custom category was imported and added to the repository
        val customCat = catRepository.getCategoryById("custom1")
        assertTrue(customCat != null)
        assertEquals("My Custom", customCat.name)
        assertEquals("star", customCat.icon)
        assertEquals("#123456", customCat.colorKey)
        assertEquals(TransactionType.Expense, customCat.type)
        assertEquals(false, customCat.isDefault)

        // Verify transactions were imported and reference correct category IDs
        assertEquals(2, txRepository.insertedTransactions.size)
        val tx1 = txRepository.insertedTransactions[0]
        assertEquals("custom1", tx1.categoryId)
        assertEquals(12000L, tx1.amount)
        assertEquals("Gift", tx1.note)

        val tx2 = txRepository.insertedTransactions[1]
        assertEquals("food", tx2.categoryId)
        assertEquals(50000L, tx2.amount)
        assertEquals("Lunch", tx2.note)
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
