package com.moneylite.core.data.repository

import com.moneylite.core.data.local.dao.BudgetDao
import com.moneylite.core.data.local.entity.BudgetEntity
import com.moneylite.core.data.mapper.toDomain
import com.moneylite.core.data.mapper.toEntity
import com.moneylite.core.domain.model.Budget
import com.moneylite.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudget(month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getBudget(month, year).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun setBudgetLimit(month: Int, year: Int, limitAmount: Long) {
        val id = "${year}-${month}"
        val entity = BudgetEntity(
            id = id,
            month = month,
            year = year,
            limitAmount = limitAmount
        )
        budgetDao.insertBudget(entity)
    }
}
