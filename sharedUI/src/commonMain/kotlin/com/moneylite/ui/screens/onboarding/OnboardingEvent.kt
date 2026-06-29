package com.moneylite.ui.screens.onboarding

/**
 * Sealed interface representing all possible UI events for the onboarding screen
 */
sealed interface OnboardingEvent {
    data object CompleteOnboarding : OnboardingEvent
    data class ToggleDarkMode(val enabled: Boolean) : OnboardingEvent
    data class NameChanged(val name: String) : OnboardingEvent
    data class JobChanged(val job: String) : OnboardingEvent
    data class SalaryChanged(val salary: String) : OnboardingEvent
    data object SaveProfileAndComplete : OnboardingEvent
}
