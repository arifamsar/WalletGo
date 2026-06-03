package com.moneylite.core.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.Octicons
import compose.icons.octicons.Calendar24
import compose.icons.octicons.CreditCard24
import compose.icons.octicons.Home24
import compose.icons.octicons.Person24
import com.moneylite.core.ui.generated.resources.Res
import com.moneylite.core.ui.generated.resources.home
import com.moneylite.core.ui.generated.resources.profile
import com.moneylite.core.ui.generated.resources.schedule
import com.moneylite.core.ui.generated.resources.transaction
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
    val label: StringResource,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: Route
) {
    HOME(
        label = Res.string.home,
        icon = Octicons.Home24,
        selectedIcon = Octicons.Home24,
        route = Route.Home
    ),
    SCHEDULE(
        label = Res.string.schedule,
        icon = Octicons.Calendar24,
        selectedIcon = Octicons.Calendar24,
        route = Route.Schedule
    ),
    TRANSACTION(
        label = Res.string.transaction,
        icon = Octicons.CreditCard24,
        selectedIcon = Octicons.CreditCard24,
        route = Route.Transaction
    ),
    PROFILE(
        label = Res.string.profile,
        icon = Octicons.Person24,
        selectedIcon = Octicons.Person24,
        route = Route.Profile
    )
}
