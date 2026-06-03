package com.moneylite.core.domain.repository

import com.moneylite.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun insertCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun seedDefaultCategories()
}
