package com.moneylite.core.data.repository

import com.moneylite.core.data.local.dao.CategoryDao
import com.moneylite.core.data.mapper.toDomain
import com.moneylite.core.data.mapper.toEntity
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) {
        categoryDao.deleteCategory(id)
    }

    override suspend fun seedDefaultCategories() {
        if (categoryDao.getCount() == 0) {
            val defaults = listOf(
                Category("food", "Food", "restaurant", "#FF9800", TransactionType.Expense, true),
                Category("transport", "Transport", "directions_car", "#2196F3", TransactionType.Expense, true),
                Category("shopping", "Shopping", "shopping_bag", "#E91E63", TransactionType.Expense, true),
                Category("bills", "Bills", "receipt", "#9C27B0", TransactionType.Expense, true),
                Category("health", "Health", "medical_services", "#E53935", TransactionType.Expense, true),
                Category("entertainment", "Entertainment", "sports_esports", "#00BCD4", TransactionType.Expense, true),
                Category("salary", "Salary", "attach_money", "#4CAF50", TransactionType.Income, true),
                Category("other", "Other", "more_horiz", "#607D8B", TransactionType.Expense, true)
            )
            categoryDao.insertCategories(defaults.map { it.toEntity() })
        }
     }

    override suspend fun deleteAllCategories() {
        categoryDao.deleteAllCategories()
    }
}
