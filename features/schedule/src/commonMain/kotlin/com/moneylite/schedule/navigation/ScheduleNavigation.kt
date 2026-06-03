package com.moneylite.schedule.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.moneylite.core.ui.navigation.Navigator
import com.moneylite.core.ui.navigation.Route
import com.moneylite.schedule.BudgetScreen

fun EntryProviderScope<NavKey>.scheduleFlow(
    navigator: Navigator,
) {
    entry<Route.Schedule> {
        BudgetScreen()
    }
}
