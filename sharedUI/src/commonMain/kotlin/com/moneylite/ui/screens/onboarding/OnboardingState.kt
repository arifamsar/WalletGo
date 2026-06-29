package com.moneylite.ui.screens.onboarding

data class OnboardingState(
    val isDarkMode: Boolean? = null,
    val isOnboardingCompleted: Boolean = false,
    val name: String = "",
    val job: String = "",
    val salary: String = "",
    val nameError: Boolean = false,
    val jobError: Boolean = false,
    val salaryError: Boolean = false
) {
    val isProfileValid: Boolean get() = name.isNotBlank() && job.isNotBlank()
    val isSalaryValid: Boolean get() = salary.isNotBlank() && salary.toLongOrNull() != null
}
