package com.moneylite.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.moneylite.core.ui.navigation.Navigator
import com.moneylite.core.ui.navigation.Route
import com.moneylite.home.HomeScreen

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
            }
        )
    }
}
