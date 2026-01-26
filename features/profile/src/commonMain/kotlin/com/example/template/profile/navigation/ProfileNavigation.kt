package com.example.template.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.template.core.ui.navigation.Navigator
import com.example.template.core.ui.navigation.Route
import com.example.template.profile.ProfileScreen
import com.example.template.profile.SettingsScreen
import com.example.template.profile.SettingsDetailScreen

fun EntryProviderScope<NavKey>.profileFlow(
    navigator: Navigator,
    onLogout: () -> Unit
) {
    entry<Route.Profile> {
        ProfileScreen(
            onNavigate = { route ->
                navigator.navigate(route)
            },
            onLogout = onLogout
        )
    }
    entry<Route.Settings> {
        SettingsScreen(
            onBack = { navigator.goBack() }
        )
    }
    entry<Route.SettingsDetail> { route ->
        SettingsDetailScreen(
            settingId = route.settingId,
            onBack = { navigator.goBack() }
        )
    }
}
