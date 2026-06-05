package com.moneylite.core.data.repository

import com.moneylite.core.data.local.dao.NotificationDao
import com.moneylite.core.data.mapper.toDomain
import com.moneylite.core.data.mapper.toEntity
import com.moneylite.core.domain.model.NotificationHistoryItem
import com.moneylite.core.domain.repository.NotificationHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationHistoryRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationHistoryRepository {

    override fun getHistory(): Flow<List<NotificationHistoryItem>> {
        return notificationDao.getNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNotification(notification: NotificationHistoryItem) {
        notificationDao.insertNotification(notification.toEntity())
    }

    override suspend fun clearHistory() {
        notificationDao.deleteAllNotifications()
    }
}
