package com.moneylite.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.moneylite.core.ui.components.LocalBottomNavigationLiquid
import com.moneylite.core.ui.navigation.Route
import com.moneylite.AppState
import com.moneylite.core.ui.navigation.rootBackTransition
import com.moneylite.core.ui.navigation.rootForwardTransition
import com.moneylite.ui.screens.MainScreen
import com.moneylite.ui.screens.forgot_password.ForgotPasswordScreen
import com.moneylite.ui.screens.forgot_password.ForgotPasswordViewModel
import com.moneylite.ui.screens.login.LoginScreen
import com.moneylite.ui.screens.login.LoginViewModel
import com.moneylite.ui.screens.onboarding.OnboardingScreen
import com.moneylite.ui.screens.splash.SplashScreen
import com.moneylite.ui.screens.splash.SplashViewModel
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppNavigation(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val rootNavigator = appState.rootNavigator
    val liquidState = rememberLiquidState()

    CompositionLocalProvider(
        value = LocalBottomNavigationLiquid provides liquidState
    ) {
        SharedTransitionLayout(
            modifier = modifier
                .liquefiable(liquidState)
        ) {
            NavDisplay(
                backStack = appState.rootBackStack,
                onBack = {
                    rootNavigator.goBack()
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                transitionSpec = rootForwardTransition(),
                popTransitionSpec = rootBackTransition(),
                entryProvider = entryProvider {
                    entry<Route.Splash> {
                        val splashViewModel = koinViewModel<SplashViewModel>()
                        val splashUiState by splashViewModel.uiState.collectAsStateWithLifecycle()

                        SplashScreen(
                            uiState = splashUiState,
                            onEvent = splashViewModel::onEvent,
                            onNavigateToHome = rootNavigator::navigateToMain,
                            onNavigateToOnboarding = rootNavigator::navigateToOnboarding,
                            onNavigateToLogin = rootNavigator::navigateToLogin
                        )
                    }
                    entry<Route.Onboarding> {
                        OnboardingScreen(
                            onOnboardingComplete = rootNavigator::navigateToLogin
                        )
                    }

                    entry<Route.Login> {
                        val viewModel = koinViewModel<LoginViewModel>()
                        val loginUiState by viewModel.uiState.collectAsStateWithLifecycle()

                        LoginScreen(
                            uiState = loginUiState,
                            onEvent = viewModel::onEvent,
                            onLoginSuccess = rootNavigator::navigateToMain,
                            navigateToForgot = {
                                rootNavigator.backStack.add(Route.ForgetPassword)
                            }
                        )
                    }

                    entry<Route.ForgetPassword> {
                        val viewModel = koinViewModel<ForgotPasswordViewModel>()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        ForgotPasswordScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onBack = {
                                rootNavigator.goBack()
                            }
                        )
                    }

                    entry<Route.Main> {
                        MainScreen()
                    }
                }
            )
        }
    }
}
