package com.moneylite.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.domain.repository.NotificationSettingsRepository
import com.moneylite.core.domain.service.NotificationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    private val settingsRepository: NotificationSettingsRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.isNotificationsEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val warningThreshold: StateFlow<Float> = settingsRepository.getWarningThresholdFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.8f)

    val dailyReminderEnabled: StateFlow<Boolean> = settingsRepository.isDailyReminderEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyReminderHour: StateFlow<Int> = settingsRepository.getDailyReminderHourFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val scheduledAlertsEnabled: StateFlow<Boolean> = settingsRepository.isScheduledAlertsEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val weeklyReportEnabled: StateFlow<Boolean> = settingsRepository.isWeeklyReportEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setWarningThreshold(threshold: Float) {
        viewModelScope.launch {
            settingsRepository.setWarningThreshold(threshold)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailyReminderEnabled(enabled)
            if (enabled) {
                val hour = settingsRepository.getDailyReminderHour()
                notificationService.scheduleRepeatingNotification(
                    id = "daily_logging_reminder",
                    title = "MoneyLite Reminder",
                    body = "Don't forget to track your spending today to stay on top of your budget!",
                    dayOfWeek = null,
                    hour = hour,
                    minute = 0,
                    interval = com.moneylite.core.domain.model.RepeatIntervalType.Daily
                )
            } else {
                notificationService.cancelNotification("daily_logging_reminder")
            }
        }
    }

    fun setDailyReminderHour(hour: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyReminderHour(hour)
            val enabled = settingsRepository.isDailyReminderEnabled()
            if (enabled) {
                notificationService.scheduleRepeatingNotification(
                    id = "daily_logging_reminder",
                    title = "MoneyLite Reminder",
                    body = "Don't forget to track your spending today to stay on top of your budget!",
                    dayOfWeek = null,
                    hour = hour,
                    minute = 0,
                    interval = com.moneylite.core.domain.model.RepeatIntervalType.Daily
                )
            }
        }
    }

    fun setScheduledAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScheduledAlertsEnabled(enabled)
        }
    }

    fun setWeeklyReportEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWeeklyReportEnabled(enabled)
            if (enabled) {
                notificationService.scheduleRepeatingNotification(
                    id = "weekly_spend_report",
                    title = "Weekly Spend Report",
                    body = "Sunday is here! Open MoneyLite to see how your budget spent is doing this week.",
                    dayOfWeek = 7, // Sunday
                    hour = 9,
                    minute = 0,
                    interval = com.moneylite.core.domain.model.RepeatIntervalType.Weekly
                )
            } else {
                notificationService.cancelNotification("weekly_spend_report")
            }
        }
    }

    fun triggerTestNotification() {
        notificationService.showInstantNotification(
            title = "Test Budget Alert!",
            body = "This is a test notification verifying your MoneyLite budget alerts work correctly."
        )
    }
}
