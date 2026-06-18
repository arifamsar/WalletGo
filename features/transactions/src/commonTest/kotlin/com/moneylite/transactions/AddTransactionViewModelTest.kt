package com.moneylite.transactions

import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAmountChange_withRawDigits_storesRawDigits() = runTest {
        val viewModel = createViewModel()

        viewModel.onAmountChange("12000")

        assertEquals("12000", viewModel.amount.value)
    }

    @Test
    fun onAmountChange_withFormattedRupiah_storesRawDigits() = runTest {
        val viewModel = createViewModel()

        viewModel.onAmountChange("Rp 1.234.567")

        assertEquals("1234567", viewModel.amount.value)
    }

    @Test
    fun saveTransaction_afterFormattedPaste_persistsCorrectAmount() = runTest {
        val repository = AddTransactionFakeTransactionRepository()
        val viewModel = createViewModel(transactionRepository = repository)
        advanceUntilIdle()

        viewModel.onAmountChange("Rp 1.234.567")
        viewModel.saveTransaction()
        advanceUntilIdle()

        assertEquals(1_234_567L, repository.insertedTransactions.single().amount)
    }

    private fun createViewModel(
        transactionRepository: AddTransactionFakeTransactionRepository = AddTransactionFakeTransactionRepository(),
        categoryRepository: AddTransactionFakeCategoryRepository = AddTransactionFakeCategoryRepository()
    ): AddTransactionViewModel {
        return AddTransactionViewModel(
            addTransactionUseCase = AddTransactionUseCase(transactionRepository),
            categoryRepository = categoryRepository,
            transactionRepository = transactionRepository
        )
    }
}

private class AddTransactionFakeTransactionRepository : TransactionRepository {
    private val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val insertedTransactions = mutableListOf<Transaction>()

    override fun getTransactions(): Flow<List<Transaction>> = transactions

    override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>> = transactions

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactions.value.firstOrNull { it.id == id }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        insertedTransactions.add(transaction)
        transactions.value = transactions.value + transaction
    }

    override suspend fun deleteTransaction(id: String) {
        transactions.value = transactions.value.filterNot { it.id == id }
    }

    override suspend fun deleteAllTransactions() {
        transactions.value = emptyList()
    }
}

private class AddTransactionFakeCategoryRepository : CategoryRepository {
    private val categories = MutableStateFlow(
        listOf(
            Category(
                id = "cat-food",
                name = "Food",
                icon = "restaurant",
                colorKey = "#607D8B",
                type = TransactionType.Expense
            )
        )
    )

    override fun getCategories(): Flow<List<Category>> = categories

    override suspend fun getCategoryById(id: String): Category? {
        return categories.value.firstOrNull { it.id == id }
    }

    override suspend fun insertCategory(category: Category) {
        categories.value = categories.value + category
    }

    override suspend fun deleteCategory(id: String) {
        categories.value = categories.value.filterNot { it.id == id }
    }

    override suspend fun seedDefaultCategories() = Unit
    override suspend fun deleteAllCategories() {
        categories.value = emptyList()
    }
}
