package com.example.template.schedule.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.template.core.ui.navigation.Navigator
import com.example.template.core.ui.navigation.Route
import com.example.template.schedule.ScheduleScreen

fun EntryProviderScope<NavKey>.scheduleFlow(
    navigator: Navigator,
) {
    entry<Route.Schedule> {
        ScheduleScreen()
    }
}
