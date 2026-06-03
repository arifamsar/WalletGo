package com.moneylite.core.data.repository

import com.moneylite.core.data.local.dao.TransactionDao
import com.moneylite.core.data.mapper.toDomain
import com.moneylite.core.data.mapper.toEntity
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>> {
        val monthPrefix = "${year}-${month.toString().padStart(2, '0')}"
        return transactionDao.getTransactionsByMonth(monthPrefix).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransaction(id)
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
}
