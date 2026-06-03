package com.moneylite.core.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import compose.icons.Octicons
import compose.icons.octicons.Calendar24
import compose.icons.octicons.CreditCard24
import compose.icons.octicons.Home24
import compose.icons.octicons.Person24
import org.jetbrains.compose.resources.StringResource
import com.moneylite.core.ui.generated.resources.Res
import com.moneylite.core.ui.generated.resources.dashboard
import com.moneylite.core.ui.generated.resources.budget
import com.moneylite.core.ui.generated.resources.profile
import com.moneylite.core.ui.generated.resources.transaction

/**
 * Data class representing a bottom navigation item
 */
data class BottomNavItem(
    val label: StringResource,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

/**
 * Mapping of top-level destinations to their corresponding BottomNavItem
 */
val TOP_LEVEL_DESTINATIONS: Map<NavKey, BottomNavItem> = mapOf(
    Route.Home to BottomNavItem(
        label = Res.string.dashboard,
        icon = Octicons.Home24,
        selectedIcon = Octicons.Home24,
    ),
    Route.Transaction to BottomNavItem(
        label = Res.string.transaction,
        icon = Octicons.CreditCard24,
        selectedIcon = Octicons.CreditCard24,
    ),
    Route.Schedule to BottomNavItem(
        label = Res.string.budget,
        icon = Octicons.Calendar24,
        selectedIcon = Octicons.Calendar24,
    ),
    Route.Profile to BottomNavItem(
        label = Res.string.profile,
        icon = Octicons.Person24,
        selectedIcon = Octicons.Person24,
    )
)
