package com.example.template.ui.di

import com.example.template.profile.ProfileViewModel
import com.example.template.ui.screens.forgot_password.ForgotPasswordViewModel
import com.example.template.ui.screens.login.LoginViewModel
import com.example.template.ui.screens.onboarding.OnboardingViewModel
import com.example.template.ui.screens.splash.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ForgotPasswordViewModel)
}
