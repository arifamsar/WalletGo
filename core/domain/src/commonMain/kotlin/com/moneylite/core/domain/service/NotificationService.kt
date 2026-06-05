package com.moneylite.core.domain.service

import com.moneylite.core.domain.model.RepeatIntervalType

interface NotificationService {
    fun showInstantNotification(title: String, body: String)
    fun scheduleSingleNotification(id: String, title: String, body: String, scheduledTimeMillis: Long)
    fun scheduleRepeatingNotification(
        id: String,
        title: String,
        body: String,
        dayOfWeek: Int?,
        hour: Int,
        minute: Int,
        interval: RepeatIntervalType
    )
    fun cancelNotification(id: String)
}

