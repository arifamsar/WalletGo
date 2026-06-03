@file:OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)

package com.moneylite.transactions.navigation

import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.moneylite.core.ui.navigation.Navigator
import com.moneylite.core.ui.navigation.Route
import com.moneylite.transactions.TransactionScreen
import com.moneylite.transactions.AddTransactionScreen

fun EntryProviderScope<NavKey>.transactionFlow(
    navigator: Navigator,
) {
    entry<Route.Transaction>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        TransactionScreen(
            onNavigateToAddTransaction = {
                navigator.navigate(Route.AddTransaction(null))
            },
            onNavigateToEditTransaction = { id ->
                navigator.navigate(Route.AddTransaction(id))
            }
        )
    }
    entry<Route.AddTransaction>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        AddTransactionScreen(
            transactionId = route.transactionId,
            onBack = { navigator.goBack() }
        )
    }
}
