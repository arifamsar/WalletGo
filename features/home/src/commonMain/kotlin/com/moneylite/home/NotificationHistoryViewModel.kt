package com.moneylite.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.domain.model.NotificationHistoryItem
import com.moneylite.core.domain.usecase.ClearNotificationHistoryUseCase
import com.moneylite.core.domain.usecase.GetNotificationHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationHistoryViewModel(
    getHistoryUseCase: GetNotificationHistoryUseCase,
    private val clearHistoryUseCase: ClearNotificationHistoryUseCase
) : ViewModel() {

    val historyItems: StateFlow<List<NotificationHistoryItem>> = getHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }
}
