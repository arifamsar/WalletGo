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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed interface BudgetEffect {
    data class CategoryBudgetDeleted(val categoryId: String, val categoryName: String) : BudgetEffect
}

class BudgetViewModel(
    private val getBudgetProgressUseCase: GetBudgetProgressUseCase,
    private val getCategoryBudgetProgressUseCase: GetCategoryBudgetProgressUseCase,
    private val budgetRepository: BudgetRepository,
    private val categoryBudgetRepository: CategoryBudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _selectedMonth = MutableStateFlow(now.month.ordinal + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val _selectedYear = MutableStateFlow(now.year)
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _budgetProgress = MutableStateFlow(BudgetProgress(0L, 0L, 0f))
    val budgetProgress: StateFlow<BudgetProgress> = _budgetProgress

    private val _categoryBudgets = MutableStateFlow<List<CategoryBudgetProgress>>(emptyList())
    val categoryBudgets: StateFlow<List<CategoryBudgetProgress>> = _categoryBudgets

    val categories = categoryRepository.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _deleteCandidate = MutableStateFlow<CategoryBudgetProgress?>(null)
    val deleteCandidate: StateFlow<CategoryBudgetProgress?> = _deleteCandidate

    private val _effects = MutableSharedFlow<BudgetEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<BudgetEffect> = _effects

    private val pendingUndoCategoryBudgets = mutableMapOf<String, Long>() // categoryId to limitAmount
    private var observeJob: Job? = null

    init {
        observeBudgetForSelectedMonth()
    }

    private fun observeBudgetForSelectedMonth() {
        observeJob?.cancel()
        val m = _selectedMonth.value
        val y = _selectedYear.value
        observeJob = viewModelScope.launch {
            launch {
                getBudgetProgressUseCase(m, y).collect { progress ->
                    _budgetProgress.value = progress
                }
            }
            launch {
                getCategoryBudgetProgressUseCase(m, y).collect { budgets ->
                    _categoryBudgets.value = budgets
                }
            }
        }
    }

    fun navigateToPreviousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
        observeBudgetForSelectedMonth()
    }

    fun navigateToNextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
        observeBudgetForSelectedMonth()
    }

    fun selectMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month.coerceIn(1, 12)
        _selectedYear.value = year
        observeBudgetForSelectedMonth()
    }

    fun updateBudgetLimit(limit: Long) {
        viewModelScope.launch {
            budgetRepository.setBudgetLimit(_selectedMonth.value, _selectedYear.value, limit)
        }
    }

    fun updateCategoryBudgetLimit(categoryId: String, limit: Long) {
        viewModelScope.launch {
            categoryBudgetRepository.setCategoryBudgetLimit(categoryId, _selectedMonth.value, _selectedYear.value, limit)
        }
    }

    fun requestDeleteCategoryBudget(categoryBudget: CategoryBudgetProgress) {
        _deleteCandidate.value = categoryBudget
    }

    fun dismissDeleteDialog() {
        _deleteCandidate.value = null
    }

    fun confirmDeleteCategoryBudget() {
        val candidate = _deleteCandidate.value ?: return
        _deleteCandidate.value = null
        viewModelScope.launch {
            val m = _selectedMonth.value
            val y = _selectedYear.value
            val id = "${candidate.categoryId}-${y}-${m}"
            categoryBudgetRepository.deleteCategoryBudget(id)
            pendingUndoCategoryBudgets[candidate.categoryId] = candidate.limitAmount
            _effects.emit(BudgetEffect.CategoryBudgetDeleted(candidate.categoryId, candidate.categoryName))
        }
    }

    fun undoDeleteCategoryBudget(categoryId: String) {
        val limit = pendingUndoCategoryBudgets.remove(categoryId) ?: return
        viewModelScope.launch {
            categoryBudgetRepository.setCategoryBudgetLimit(categoryId, _selectedMonth.value, _selectedYear.value, limit)
        }
    }

    fun clearPendingUndo(categoryId: String) {
        pendingUndoCategoryBudgets.remove(categoryId)
    }
}
