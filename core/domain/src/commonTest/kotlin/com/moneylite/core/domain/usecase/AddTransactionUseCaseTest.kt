package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakeTransactionRepository : TransactionRepository {
    val insertedTransactions = mutableListOf<Transaction>()

    override fun getTransactions(): Flow<List<Transaction>> = TODO()
    override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>> = TODO()
    override suspend fun getTransactionById(id: String): Transaction? = TODO()

    override suspend fun insertTransaction(transaction: Transaction) {
        insertedTransactions.add(transaction)
    }

    override suspend fun deleteTransaction(id: String) = TODO()
    override suspend fun deleteAllTransactions() = TODO()
}

class AddTransactionUseCaseTest {

    private val repository = FakeTransactionRepository()
    private val useCase = AddTransactionUseCase(repository)

    @Test
    fun testInsert_negativeAmount_returnsFailure() = runTest {
        val tx = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = -100L,
            categoryId = "food",
            note = "Negative test",
            date = LocalDate(2026, 6, 3),
            createdAt = Clock.System.now()
        )
        val result = useCase(tx)
        assertTrue(result.isFailure)
        assertTrue(repository.insertedTransactions.isEmpty())
    }

    @Test
    fun testInsert_zeroAmount_returnsFailure() = runTest {
        val tx = Transaction(
            id = "2",
            type = TransactionType.Expense,
            amount = 0L,
            categoryId = "food",
            note = "Zero test",
            date = LocalDate(2026, 6, 3),
            createdAt = Clock.System.now()
        )
        val result = useCase(tx)
        assertTrue(result.isFailure)
        assertTrue(repository.insertedTransactions.isEmpty())
    }

    @Test
    fun testInsert_positiveAmount_returnsSuccessAndInserts() = runTest {
        val tx = Transaction(
            id = "3",
            type = TransactionType.Expense,
            amount = 15000L,
            categoryId = "food",
            note = "Bakso Kupang",
            date = LocalDate(2026, 6, 3),
            createdAt = Clock.System.now()
        )
        val result = useCase(tx)
        assertTrue(result.isSuccess)
        assertFalse(repository.insertedTransactions.isEmpty())
        assertTrue(repository.insertedTransactions.contains(tx))
    }
}
