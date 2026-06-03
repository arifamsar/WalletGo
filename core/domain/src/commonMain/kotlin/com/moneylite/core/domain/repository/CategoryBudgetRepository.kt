package com.moneylite.core.domain.repository

import com.moneylite.core.domain.model.CategoryBudget
import kotlinx.coroutines.flow.Flow

interface CategoryBudgetRepository {
    fun getCategoryBudgets(month: Int, year: Int): Flow<List<CategoryBudget>>
    fun getCategoryBudget(categoryId: String, month: Int, year: Int): Flow<CategoryBudget?>
    suspend fun setCategoryBudgetLimit(categoryId: String, month: Int, year: Int, limitAmount: Long)
    suspend fun deleteCategoryBudget(id: String)
}
