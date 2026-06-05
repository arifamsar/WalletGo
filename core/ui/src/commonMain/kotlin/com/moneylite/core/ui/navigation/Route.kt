package com.moneylite.core.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Splash : Route

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Login : Route

    // Main Flow (with Bottom Navigation) - These require login
    @Serializable
    data object Main : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Schedule : Route

    @Serializable
    data object Transaction : Route

    @Serializable
    data class AddTransaction(val transactionId: String? = null) : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object ThemeSettings : Route

    @Serializable
    data class SettingsDetail(val settingId: String) : Route

    @Serializable
    data object ForgetPassword : Route

    @Serializable
    data object NotificationHistory : Route

    @Serializable
    data object NotificationSettings : Route
}

