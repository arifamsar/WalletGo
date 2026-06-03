package com.moneylite.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.parseRupiahToLong
import com.moneylite.core.domain.model.rupiahDigits
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _type = MutableStateFlow(TransactionType.Expense)
    val type = _type.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _date = MutableStateFlow(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    val date = _date.asStateFlow()

    private val _note = MutableStateFlow("")
    val note = _note.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val filteredCategories = combine(_categories, _type) { categories, type ->
        categories.filter { it.type == type }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { cats ->
                _categories.value = cats
                // Set default selected category if none selected
                if (_selectedCategoryId.value == null) {
                    val defaultCat = cats.firstOrNull { it.type == _type.value }
                    _selectedCategoryId.value = defaultCat?.id
                }
            }
        }
    }

    private var editingTransaction: Transaction? = null

    fun loadTransaction(id: String) {
        // Only load if not already loaded to avoid resetting state on recomposition
        if (editingTransaction?.id == id) return
        viewModelScope.launch {
            transactionRepository.getTransactionById(id)?.let { tx ->
                editingTransaction = tx
                _amount.value = tx.amount.toString()
                _type.value = tx.type
                _selectedCategoryId.value = tx.categoryId
                _date.value = tx.date
                _note.value = tx.note ?: ""
            }
        }
    }

    fun onAmountChange(value: String) {
        _amount.value = value.rupiahDigits()
    }

    fun onTypeChange(value: TransactionType) {
        _type.value = value
        val matchingCat = _categories.value.firstOrNull { it.type == value }
        _selectedCategoryId.value = matchingCat?.id
    }

    fun onCategorySelect(id: String) {
        _selectedCategoryId.value = id
    }

    fun onDateChange(localDate: LocalDate) {
        _date.value = localDate
    }

    fun onNoteChange(value: String) {
        _note.value = value
    }

    fun saveTransaction() {
        val amt = _amount.value.parseRupiahToLong()
        if (amt == null || amt <= 0) {
            _error.value = "Please enter a valid amount"
            return
        }
        val catId = _selectedCategoryId.value
        if (catId == null) {
            _error.value = "Please select a category"
            return
        }

        viewModelScope.launch {
            val transaction = editingTransaction?.copy(
                type = _type.value,
                amount = amt,
                categoryId = catId,
                note = _note.value.ifBlank { null },
                date = _date.value
            ) ?: Transaction(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                type = _type.value,
                amount = amt,
                categoryId = catId,
                note = _note.value.ifBlank { null },
                date = _date.value,
                createdAt = Clock.System.now()
            )

            addTransactionUseCase(transaction)
                .onSuccess {
                    _isSaved.value = true
                }
                .onFailure {
                    _error.value = it.message ?: "Failed to save transaction"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
