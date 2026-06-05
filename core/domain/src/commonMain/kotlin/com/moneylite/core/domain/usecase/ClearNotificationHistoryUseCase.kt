package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.repository.NotificationHistoryRepository

class ClearNotificationHistoryUseCase(
    private val notificationHistoryRepository: NotificationHistoryRepository
) {
    suspend operator fun invoke() {
        notificationHistoryRepository.clearHistory()
    }
}
