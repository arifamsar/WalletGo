package com.moneylite.core.domain.repository

import com.moneylite.core.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudget(month: Int, year: Int): Flow<Budget?>
    suspend fun insertBudget(budget: Budget)
    suspend fun setBudgetLimit(month: Int, year: Int, limitAmount: Long)
}
