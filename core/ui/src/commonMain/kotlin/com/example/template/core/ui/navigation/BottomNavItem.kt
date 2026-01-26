package com.example.template.core.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.example.template.core.ui.components.icons.CalenderFilled
import com.example.template.core.ui.components.icons.CalenderOutlined
import com.example.template.core.ui.components.icons.Hicon
import com.example.template.core.ui.components.icons.HomeFilled
import com.example.template.core.ui.components.icons.HomeOutlined
import com.example.template.core.ui.components.icons.ProfileFilled
import com.example.template.core.ui.components.icons.ProfileOutlined
import com.example.template.core.ui.components.icons.WalletFilled
import com.example.template.core.ui.components.icons.WalletOutlined
import org.jetbrains.compose.resources.StringResource
import com.example.template.core.ui.generated.resources.Res
import com.example.template.core.ui.generated.resources.home
import com.example.template.core.ui.generated.resources.profile
import com.example.template.core.ui.generated.resources.schedule
import com.example.template.core.ui.generated.resources.transaction

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
        label = Res.string.home,
        icon = Hicon.HomeOutlined,
        selectedIcon = Hicon.HomeFilled,
    ),
    Route.Schedule to BottomNavItem(
        label = Res.string.schedule,
        icon = Hicon.CalenderOutlined,
        selectedIcon = Hicon.CalenderFilled,
    ),
    Route.Transaction to BottomNavItem(
        label = Res.string.transaction,
        icon = Hicon.WalletOutlined,
        selectedIcon = Hicon.WalletFilled,
    ),
    Route.Profile to BottomNavItem(
        label = Res.string.profile,
        icon = Hicon.ProfileOutlined,
        selectedIcon = Hicon.ProfileFilled,
    )
)
