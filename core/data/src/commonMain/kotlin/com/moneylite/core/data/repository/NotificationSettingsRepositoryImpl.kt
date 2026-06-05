package com.moneylite.core.data.repository

import com.moneylite.core.data.service.UserPreferences
import com.moneylite.core.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow

class NotificationSettingsRepositoryImpl(
    private val userPreferences: UserPreferences
) : NotificationSettingsRepository {

    override fun isNotificationsEnabledFlow(): Flow<Boolean> = userPreferences.notificationsEnabledFlow()

    override suspend fun isNotificationsEnabled(): Boolean = userPreferences.isNotificationsEnabled()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        userPreferences.setNotificationsEnabled(enabled)
    }

    override fun getWarningThresholdFlow(): Flow<Float> = userPreferences.warningThresholdFlow()

    override suspend fun getWarningThreshold(): Float = userPreferences.getWarningThreshold()

    override suspend fun setWarningThreshold(threshold: Float) {
        userPreferences.setWarningThreshold(threshold)
    }

    override fun isDailyReminderEnabledFlow(): Flow<Boolean> = userPreferences.dailyReminderEnabledFlow()

    override suspend fun isDailyReminderEnabled(): Boolean = userPreferences.isDailyReminderEnabled()

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        userPreferences.setDailyReminderEnabled(enabled)
    }

    override fun getDailyReminderHourFlow(): Flow<Int> = userPreferences.dailyReminderHourFlow()

    override suspend fun getDailyReminderHour(): Int = userPreferences.getDailyReminderHour()

    override suspend fun setDailyReminderHour(hour: Int) {
        userPreferences.setDailyReminderHour(hour)
    }

    override fun isScheduledAlertsEnabledFlow(): Flow<Boolean> = userPreferences.scheduledAlertsEnabledFlow()

    override suspend fun isScheduledAlertsEnabled(): Boolean = userPreferences.isScheduledAlertsEnabled()

    override suspend fun setScheduledAlertsEnabled(enabled: Boolean) {
        userPreferences.setScheduledAlertsEnabled(enabled)
    }

    override fun isWeeklyReportEnabledFlow(): Flow<Boolean> = userPreferences.weeklyReportEnabledFlow()

    override suspend fun isWeeklyReportEnabled(): Boolean = userPreferences.isWeeklyReportEnabled()

    override suspend fun setWeeklyReportEnabled(enabled: Boolean) {
        userPreferences.setWeeklyReportEnabled(enabled)
    }
}

