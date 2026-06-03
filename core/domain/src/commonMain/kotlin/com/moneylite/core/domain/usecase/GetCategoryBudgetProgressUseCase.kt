package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.CategoryBudgetRepository
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CategoryBudgetProgress(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val limitAmount: Long,
    val usedAmount: Long,
    val progress: Float
)

class GetCategoryBudgetProgressUseCase(
    private val categoryBudgetRepository: CategoryBudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<List<CategoryBudgetProgress>> {
        return combine(
            categoryBudgetRepository.getCategoryBudgets(month, year),
            categoryRepository.getCategories(),
            transactionRepository.getTransactionsByMonth(month, year)
        ) { categoryBudgets, categories, transactions ->
            val budgetMap = categoryBudgets.associateBy { it.categoryId }
            val expensesByCategory = transactions
                .filter { it.type == TransactionType.Expense }
                .groupBy { it.categoryId }

            categoryBudgets.mapNotNull { budget ->
                val category = categories.find { it.id == budget.categoryId }
                if (category != null) {
                    val spent = expensesByCategory[budget.categoryId]?.sumOf { it.amount } ?: 0L
                    val limit = budget.limitAmount
                    val progress = if (limit > 0) (spent.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
                    CategoryBudgetProgress(
                        categoryId = budget.categoryId,
                        categoryName = category.name,
                        categoryIcon = category.icon,
                        categoryColor = category.colorKey,
                        limitAmount = limit,
                        usedAmount = spent,
                        progress = progress
                    )
                } else null
            }
        }
    }
}
