package com.example.template.transactions.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.template.core.ui.navigation.Navigator
import com.example.template.core.ui.navigation.Route
import com.example.template.transactions.TransactionScreen

fun EntryProviderScope<NavKey>.transactionFlow(
    navigator: Navigator,
) {
    entry<Route.Transaction> {
        TransactionScreen()
    }
}
