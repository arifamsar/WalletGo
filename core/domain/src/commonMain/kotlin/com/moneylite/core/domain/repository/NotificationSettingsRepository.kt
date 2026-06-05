package com.moneylite.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun isNotificationsEnabledFlow(): Flow<Boolean>
    suspend fun isNotificationsEnabled(): Boolean
    suspend fun setNotificationsEnabled(enabled: Boolean)

    fun getWarningThresholdFlow(): Flow<Float>
    suspend fun getWarningThreshold(): Float
    suspend fun setWarningThreshold(threshold: Float)

    fun isDailyReminderEnabledFlow(): Flow<Boolean>
    suspend fun isDailyReminderEnabled(): Boolean
    suspend fun setDailyReminderEnabled(enabled: Boolean)

    fun getDailyReminderHourFlow(): Flow<Int>
    suspend fun getDailyReminderHour(): Int
    suspend fun setDailyReminderHour(hour: Int)

    fun isScheduledAlertsEnabledFlow(): Flow<Boolean>
    suspend fun isScheduledAlertsEnabled(): Boolean
    suspend fun setScheduledAlertsEnabled(enabled: Boolean)

    fun isWeeklyReportEnabledFlow(): Flow<Boolean>
    suspend fun isWeeklyReportEnabled(): Boolean
    suspend fun setWeeklyReportEnabled(enabled: Boolean)
}

