package com.moneylite.transactions

import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

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
    fun search_matchesCategoryNameAndNote_ignoringCaseAndWhitespace() = runTest {
        val viewModel = createViewModel(
            transactions = listOf(
                transaction(id = "food", categoryId = "cat-food", note = "Lunch"),
                transaction(id = "salary", type = TransactionType.Income, categoryId = "cat-salary", note = null),
                transaction(id = "taxi", categoryId = "cat-transport", note = "Airport ride")
            )
        )
        advanceUntilIdle()

        viewModel.onIntent(TransactionListIntent.SearchQueryChanged("  salary  "))
        advanceUntilIdle()

        assertEquals(listOf("salary"), viewModel.uiState.value.transactions.map { it.id })

        viewModel.onIntent(TransactionListIntent.SearchQueryChanged("AIRPORT"))
        advanceUntilIdle()

        assertEquals(listOf("taxi"), viewModel.uiState.value.transactions.map { it.id })
    }

    @Test
    fun search_combinesWithTypeFilter() = runTest {
        val viewModel = createViewModel(
            transactions = listOf(
                transaction(id = "expense-food", type = TransactionType.Expense, categoryId = "cat-food"),
                transaction(id = "income-food", type = TransactionType.Income, categoryId = "cat-food")
            )
        )
        advanceUntilIdle()

        viewModel.onIntent(TransactionListIntent.SearchQueryChanged("food"))
        viewModel.onIntent(TransactionListIntent.TypeFilterSelected(TransactionType.Income))
        advanceUntilIdle()

        assertEquals(listOf("income-food"), viewModel.uiState.value.transactions.map { it.id })
    }

    @Test
    fun deleteRequestAndDismiss_updatesDialogCandidate() = runTest {
        val viewModel = createViewModel(transactions = listOf(transaction(id = "tx-1")))
        advanceUntilIdle()
        val transaction = viewModel.uiState.value.transactions.first()

        viewModel.onIntent(TransactionListIntent.RequestDeleteTransaction(transaction))

        assertEquals(transaction, viewModel.uiState.value.deleteCandidate)

        viewModel.onIntent(TransactionListIntent.DismissDeleteDialog)

        assertNull(viewModel.uiState.value.deleteCandidate)
    }

    @Test
    fun confirmDelete_deletesAndEmitsUndoSnackbarEffect() = runTest {
        val repository = FakeTransactionRepository(listOf(transaction(id = "tx-1")))
        val viewModel = createViewModel(transactionRepository = repository)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }
        val transaction = viewModel.uiState.value.transactions.first()

        viewModel.onIntent(TransactionListIntent.RequestDeleteTransaction(transaction))
        viewModel.onIntent(TransactionListIntent.ConfirmDeleteTransaction)
        advanceUntilIdle()

        assertEquals(TransactionListEffect.TransactionDeleted("tx-1"), effect.await())
        assertEquals(listOf("tx-1"), repository.deletedIds)
        assertNull(viewModel.uiState.value.deleteCandidate)
    }

    @Test
    fun undoDelete_reinsertsExactCachedTransaction() = runTest {
        val deletedTransaction = transaction(id = "tx-1", note = "Original note")
        val repository = FakeTransactionRepository(listOf(deletedTransaction))
        val viewModel = createViewModel(transactionRepository = repository)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }
        val transaction = viewModel.uiState.value.transactions.first()

        viewModel.onIntent(TransactionListIntent.RequestDeleteTransaction(transaction))
        viewModel.onIntent(TransactionListIntent.ConfirmDeleteTransaction)
        advanceUntilIdle()
        effect.await()

        viewModel.onIntent(TransactionListIntent.UndoDeleteTransaction("tx-1"))
        advanceUntilIdle()

        assertTrue(repository.insertedTransactions.contains(deletedTransaction))
    }

    private fun createViewModel(
        transactions: List<Transaction> = listOf(transaction()),
        categories: List<Category> = defaultCategories,
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(transactions),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(categories)
    ): TransactionViewModel {
        return TransactionViewModel(
            getTransactionsUseCase = GetTransactionsUseCase(transactionRepository),
            categoryRepository = categoryRepository,
            transactionRepository = transactionRepository
        )
    }
}

private class FakeTransactionRepository(
    transactions: List<Transaction>
) : TransactionRepository {
    private val transactions = MutableStateFlow(transactions)
    val deletedIds = mutableListOf<String>()
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
        deletedIds.add(id)
        transactions.value = transactions.value.filterNot { it.id == id }
    }

    override suspend fun deleteAllTransactions() {
        transactions.value = emptyList()
    }
}

private class FakeCategoryRepository(
    categories: List<Category>
) : CategoryRepository {
    private val categories = MutableStateFlow(categories)

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
}

private val defaultCategories = listOf(
    category(id = "cat-food", name = "Food", type = TransactionType.Expense),
    category(id = "cat-salary", name = "Salary", type = TransactionType.Income),
    category(id = "cat-transport", name = "Transport", type = TransactionType.Expense)
)

private fun transaction(
    id: String = "tx-1",
    type: TransactionType = TransactionType.Expense,
    categoryId: String = "cat-food",
    note: String? = null
) = Transaction(
    id = id,
    type = type,
    amount = 12_000L,
    categoryId = categoryId,
    note = note,
    date = LocalDate(2026, 6, 3),
    createdAt = Instant.parse("2026-06-03T00:00:00Z")
)

private fun category(
    id: String,
    name: String,
    type: TransactionType
) = Category(
    id = id,
    name = name,
    icon = "more_horiz",
    colorKey = "#607D8B",
    type = type
)
