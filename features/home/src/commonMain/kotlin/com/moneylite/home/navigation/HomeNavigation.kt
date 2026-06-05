@file:OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)

package com.moneylite.home.navigation

import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.moneylite.core.ui.navigation.Navigator
import com.moneylite.core.ui.navigation.Route
import com.moneylite.home.HomeScreen
import com.moneylite.home.NotificationHistoryScreen

fun EntryProviderScope<NavKey>.homeFlow(
    navigator: Navigator,
) {
    entry<Route.Home> {
        HomeScreen(
            onNavigateToAddTransaction = {
                navigator.navigateInTopLevelStack(Route.Transaction, Route.AddTransaction(null))
            },
            onNavigateToTransactions = {
                navigator.navigate(Route.Transaction)
            },
            onNavigateToEditTransaction = { id ->
                navigator.navigateInTopLevelStack(Route.Transaction, Route.AddTransaction(id))
            },
            onNavigateToNotificationHistory = {
                navigator.navigate(Route.NotificationHistory)
            }
        )
    }
    entry<Route.NotificationHistory>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        NotificationHistoryScreen(
            onBack = { navigator.goBack() }
        )
    }
}

