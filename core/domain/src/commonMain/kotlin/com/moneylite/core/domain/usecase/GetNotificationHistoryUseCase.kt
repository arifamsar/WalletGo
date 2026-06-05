package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.NotificationHistoryItem
import com.moneylite.core.domain.repository.NotificationHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationHistoryUseCase(
    private val notificationHistoryRepository: NotificationHistoryRepository
) {
    operator fun invoke(): Flow<List<NotificationHistoryItem>> {
        return notificationHistoryRepository.getHistory()
    }
}
