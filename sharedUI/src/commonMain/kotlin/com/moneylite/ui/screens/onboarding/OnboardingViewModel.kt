package com.moneylite.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.data.service.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        observeDarkMode()
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.CompleteOnboarding -> completeOnboarding()
            is OnboardingEvent.ToggleDarkMode -> toggleDarkMode(event.enabled)
            is OnboardingEvent.NameChanged -> _state.update { it.copy(name = event.name, nameError = event.name.isBlank()) }
            is OnboardingEvent.JobChanged -> _state.update { it.copy(job = event.job, jobError = event.job.isBlank()) }
            is OnboardingEvent.SalaryChanged -> _state.update { it.copy(salary = event.salary, salaryError = event.salary.toLongOrNull() == null) }
            is OnboardingEvent.SaveProfileAndComplete -> saveProfileAndComplete()
        }
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            userPreferences.darkModeEnabledFlow().collect { isEnabled ->
                _state.update { it.copy(isDarkMode = isEnabled) }
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setUserName("User")
            userPreferences.setUserJob("Freelancer")
            userPreferences.setUserSalary(0L)
            userPreferences.setOnboardingCompleted(true)
            _state.update { it.copy(isOnboardingCompleted = true) }
        }
    }

    private fun saveProfileAndComplete() {
        viewModelScope.launch {
            val currentState = _state.value
            val nameErr = currentState.name.isBlank()
            val jobErr = currentState.job.isBlank()
            val salaryErr = currentState.salary.toLongOrNull() == null

            if (nameErr || jobErr || salaryErr) {
                _state.update { it.copy(nameError = nameErr, jobError = jobErr, salaryError = salaryErr) }
            } else {
                userPreferences.setUserName(currentState.name)
                userPreferences.setUserJob(currentState.job)
                userPreferences.setUserSalary(currentState.salary.toLongOrNull() ?: 0L)
                userPreferences.setOnboardingCompleted(true)
                _state.update { it.copy(isOnboardingCompleted = true) }
            }
        }
    }

    private fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
        }
    }
}
