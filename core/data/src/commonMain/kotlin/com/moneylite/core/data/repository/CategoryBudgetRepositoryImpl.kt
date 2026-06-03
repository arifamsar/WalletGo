package com.moneylite.core.data.repository

import com.moneylite.core.data.local.dao.CategoryBudgetDao
import com.moneylite.core.data.local.entity.CategoryBudgetEntity
import com.moneylite.core.data.mapper.toDomain
import com.moneylite.core.domain.model.CategoryBudget
import com.moneylite.core.domain.repository.CategoryBudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryBudgetRepositoryImpl(
    private val categoryBudgetDao: CategoryBudgetDao
) : CategoryBudgetRepository {

    override fun getCategoryBudgets(month: Int, year: Int): Flow<List<CategoryBudget>> {
        return categoryBudgetDao.getCategoryBudgets(month, year).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCategoryBudget(categoryId: String, month: Int, year: Int): Flow<CategoryBudget?> {
        return categoryBudgetDao.getCategoryBudget(categoryId, month, year).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun setCategoryBudgetLimit(categoryId: String, month: Int, year: Int, limitAmount: Long) {
        val id = "${categoryId}-${year}-${month}"
        val entity = CategoryBudgetEntity(
            id = id,
            categoryId = categoryId,
            month = month,
            year = year,
            limitAmount = limitAmount
        )
        categoryBudgetDao.insertCategoryBudget(entity)
    }

    override suspend fun deleteCategoryBudget(id: String) {
        categoryBudgetDao.deleteCategoryBudget(id)
    }
}
