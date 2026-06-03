@file:OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)

package com.moneylite.profile.navigation

import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.Text
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.moneylite.core.ui.navigation.Navigator
import com.moneylite.core.ui.navigation.Route
import com.moneylite.profile.ProfileScreen
import com.moneylite.profile.SettingsScreen
import com.moneylite.profile.SettingsDetailScreen
import com.moneylite.profile.ThemeSettingsScreen

fun EntryProviderScope<NavKey>.profileFlow(
    navigator: Navigator
) {
    entry<Route.Profile>(
        metadata = ListDetailSceneStrategy.listPane(
            detailPlaceholder = {
                Text("Choose a setting")
            }
        )
    ) {
        ProfileScreen(
            onNavigate = { route ->
                navigator.navigate(route)
            }
        )
    }
    entry<Route.Settings>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        SettingsScreen(
            onBack = { navigator.goBack() },
            onThemeSettings = { navigator.navigate(Route.ThemeSettings) }
        )
    }
    entry<Route.ThemeSettings>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        ThemeSettingsScreen(
            onBack = { navigator.goBack() }
        )
    }
    entry<Route.SettingsDetail>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        SettingsDetailScreen(
            settingId = route.settingId,
            onBack = { navigator.goBack() }
        )
    }
}
