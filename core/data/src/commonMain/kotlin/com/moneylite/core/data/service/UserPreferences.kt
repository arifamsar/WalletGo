package com.moneylite.core.data.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import com.moneylite.core.ui.theme.ThemeTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[onboarding_completed] = completed
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[onboarding_completed] ?: false
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[is_logged_in] = loggedIn
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[is_logged_in] ?: false
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[is_dark_mode] = enabled
        }
    }

    suspend fun isDarkModeEnabled(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[is_dark_mode] ?: false
    }

    fun darkModeEnabledFlow(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[is_dark_mode] ?: false }

    suspend fun setThemeTemplate(template: ThemeTemplate) {
        dataStore.edit { preferences ->
            preferences[theme_template] = template.id
        }
    }

    fun themeTemplateFlow(): Flow<ThemeTemplate> =
        dataStore.data.map { preferences ->
            ThemeTemplate.fromId(preferences[theme_template])
        }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[user_name] = name
        }
    }

    suspend fun getUserName(): String {
        val preferences = dataStore.data.first()
        return preferences[user_name] ?: "User"
    }

    fun userNameFlow(): Flow<String> =
        dataStore.data.map { preferences -> preferences[user_name] ?: "User" }

    suspend fun setUserJob(job: String) {
        dataStore.edit { preferences ->
            preferences[user_job] = job
        }
    }

    suspend fun getUserJob(): String {
        val preferences = dataStore.data.first()
        return preferences[user_job] ?: "Freelancer"
    }

    fun userJobFlow(): Flow<String> =
        dataStore.data.map { preferences -> preferences[user_job] ?: "Freelancer" }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[notifications_enabled] = enabled
        }
    }

    suspend fun isNotificationsEnabled(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[notifications_enabled] ?: true
    }

    fun notificationsEnabledFlow(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[notifications_enabled] ?: true }

    suspend fun setWarningThreshold(threshold: Float) {
        dataStore.edit { preferences ->
            preferences[warning_threshold] = threshold
        }
    }

    suspend fun getWarningThreshold(): Float {
        val preferences = dataStore.data.first()
        return preferences[warning_threshold] ?: 0.8f
    }

    fun warningThresholdFlow(): Flow<Float> =
        dataStore.data.map { preferences -> preferences[warning_threshold] ?: 0.8f }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[daily_reminder_enabled] = enabled
        }
    }

    suspend fun isDailyReminderEnabled(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[daily_reminder_enabled] ?: true
    }

    fun dailyReminderEnabledFlow(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[daily_reminder_enabled] ?: true }

    suspend fun setDailyReminderHour(hour: Int) {
        dataStore.edit { preferences ->
            preferences[daily_reminder_hour] = hour
        }
    }

    suspend fun getDailyReminderHour(): Int {
        val preferences = dataStore.data.first()
        return preferences[daily_reminder_hour] ?: 20
    }

    fun dailyReminderHourFlow(): Flow<Int> =
        dataStore.data.map { preferences -> preferences[daily_reminder_hour] ?: 20 }

    suspend fun setScheduledAlertsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[scheduled_alerts_enabled] = enabled
        }
    }

    suspend fun isScheduledAlertsEnabled(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[scheduled_alerts_enabled] ?: true
    }

    fun scheduledAlertsEnabledFlow(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[scheduled_alerts_enabled] ?: true }

    suspend fun setWeeklyReportEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[weekly_report_enabled] = enabled
        }
    }

    suspend fun isWeeklyReportEnabled(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[weekly_report_enabled] ?: true
    }

    fun weeklyReportEnabledFlow(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[weekly_report_enabled] ?: true }

    companion object {
        val onboarding_completed = booleanPreferencesKey("onboarding_completed")
        val is_logged_in = booleanPreferencesKey("is_logged_in")
        val is_dark_mode = booleanPreferencesKey("is_dark_mode")
        val theme_template = stringPreferencesKey("theme_template")
        val user_name = stringPreferencesKey("user_name")
        val user_job = stringPreferencesKey("user_job")
        val notifications_enabled = booleanPreferencesKey("notifications_enabled")
        val warning_threshold = floatPreferencesKey("warning_threshold")
        val daily_reminder_enabled = booleanPreferencesKey("daily_reminder_enabled")
        val daily_reminder_hour = intPreferencesKey("daily_reminder_hour")
        val scheduled_alerts_enabled = booleanPreferencesKey("scheduled_alerts_enabled")
        val weekly_report_enabled = booleanPreferencesKey("weekly_report_enabled")
    }
}


