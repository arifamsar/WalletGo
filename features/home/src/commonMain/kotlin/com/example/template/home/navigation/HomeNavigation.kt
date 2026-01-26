package com.example.template.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.template.core.ui.navigation.Navigator
import com.example.template.core.ui.navigation.Route
import com.example.template.home.HomeScreen

fun EntryProviderScope<NavKey>.homeFlow(
    navigator: Navigator,
) {
    entry<Route.Home> {
        HomeScreen()
    }
}
