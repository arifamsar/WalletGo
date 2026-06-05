package com.moneylite.core.domain.repository

import com.moneylite.core.domain.model.NotificationHistoryItem
import kotlinx.coroutines.flow.Flow

interface NotificationHistoryRepository {
    fun getHistory(): Flow<List<NotificationHistoryItem>>
    suspend fun saveNotification(notification: NotificationHistoryItem)
    suspend fun clearHistory()
}
