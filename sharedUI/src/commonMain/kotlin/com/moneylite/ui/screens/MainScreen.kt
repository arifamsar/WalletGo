package com.moneylite.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.moneylite.core.ui.components.AppNavigationBar
import com.moneylite.core.ui.components.AppNavigationRail
import com.moneylite.core.ui.components.LocalBottomNavigationLiquid
import com.moneylite.core.ui.navigation.Navigator
import com.moneylite.core.ui.navigation.Route
import com.moneylite.core.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.moneylite.core.ui.navigation.mainBackTransition
import com.moneylite.core.ui.navigation.mainForwardTransition
import com.moneylite.core.ui.navigation.rememberNavigationState
import com.moneylite.core.ui.navigation.toEntries
import com.moneylite.home.navigation.homeFlow
import com.moneylite.profile.navigation.profileFlow
import com.moneylite.schedule.navigation.scheduleFlow
import com.moneylite.transactions.navigation.transactionFlow
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val liquidState = rememberLiquidState()
    val navigationState = rememberNavigationState(
        startRoute = Route.Home,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember(navigationState) {
        Navigator(navigationState)
    }

    CompositionLocalProvider(
        LocalBottomNavigationLiquid provides liquidState
    ) {
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

        BoxWithConstraints(modifier = modifier) {
            val isTablet = maxWidth >= 600.dp

            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .liquefiable(liquidState)
                ) {
                    if (isTablet) {
                        AnimatedVisibility(
                            visible = navigationState.isOnTopLevelDestination,
                            enter = slideInHorizontally { -it },
                            exit = slideOutHorizontally { -it }
                        ) {
                            AppNavigationRail(
                                selectedKey = navigationState.topLevelRoute,
                                onSelectKey = navigator::navigate
                            )
                        }
                    }

                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            if (!isTablet) {
                                AnimatedVisibility(
                                    visible = navigationState.isOnTopLevelDestination,
                                    enter = slideInVertically { it },
                                    exit = slideOutVertically { it }
                                ) {
                                    AppNavigationBar(
                                        selectedKey = navigationState.topLevelRoute,
                                        onSelectKey = navigator::navigate
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavDisplay(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            onBack = navigator::goBack,
                            sceneStrategy = listDetailStrategy,
                            transitionSpec = mainForwardTransition(),
                            popTransitionSpec = mainBackTransition(),
                            entries = navigationState.toEntries(
                                entryProvider {
                                    homeFlow(navigator = navigator)
                                    scheduleFlow(navigator = navigator)
                                    transactionFlow(navigator = navigator)
                                    profileFlow(navigator = navigator)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
