package com.moneylite.ui.di

import com.moneylite.home.DashboardViewModel
import com.moneylite.profile.ProfileViewModel
import com.moneylite.schedule.BudgetViewModel
import com.moneylite.transactions.TransactionViewModel
import com.moneylite.transactions.AddTransactionViewModel
import com.moneylite.ui.screens.forgot_password.ForgotPasswordViewModel
import com.moneylite.ui.screens.login.LoginViewModel
import com.moneylite.ui.screens.onboarding.OnboardingViewModel
import com.moneylite.ui.screens.splash.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::TransactionViewModel)
    viewModelOf(::AddTransactionViewModel)
    viewModelOf(::BudgetViewModel)
}
