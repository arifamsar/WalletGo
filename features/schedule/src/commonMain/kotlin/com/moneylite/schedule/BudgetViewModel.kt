package com.moneylite.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.domain.repository.BudgetRepository
import com.moneylite.core.domain.repository.CategoryBudgetRepository
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.usecase.BudgetProgress
import com.moneylite.core.domain.usecase.GetBudgetProgressUseCase
import com.moneylite.core.domain.usecase.GetCategoryBudgetProgressUseCase
import com.moneylite.core.domain.usecase.CategoryBudgetProgress
import com.moneylite.core.domain.model.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class BudgetViewModel(
    private val getBudgetProgressUseCase: GetBudgetProgressUseCase,
    private val getCategoryBudgetProgressUseCase: GetCategoryBudgetProgressUseCase,
    private val budgetRepository: BudgetRepository,
    private val categoryBudgetRepository: CategoryBudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val month = now.month.ordinal + 1
    private val year = now.year

    val budgetProgress = getBudgetProgressUseCase(month, year)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetProgress(0L, 0L, 0f)
        )

    val categoryBudgets = getCategoryBudgetProgressUseCase(month, year)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories = categoryRepository.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateBudgetLimit(limit: Long) {
        viewModelScope.launch {
            budgetRepository.setBudgetLimit(month, year, limit)
        }
    }

    fun updateCategoryBudgetLimit(categoryId: String, limit: Long) {
        viewModelScope.launch {
            categoryBudgetRepository.setCategoryBudgetLimit(categoryId, month, year, limit)
        }
    }

    fun deleteCategoryBudget(categoryId: String) {
        viewModelScope.launch {
            val id = "${categoryId}-${year}-${month}"
            categoryBudgetRepository.deleteCategoryBudget(id)
        }
    }
}
