package com.moneylite.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneylite.core.data.service.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(private val userPreferences: UserPreferences): ViewModel() {

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userJob = MutableStateFlow("Freelancer")
    val userJob: StateFlow<String> = _userJob.asStateFlow()

    private val _userSalary = MutableStateFlow(0L)
    val userSalary: StateFlow<Long> = _userSalary.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.darkModeEnabledFlow().collectLatest { enabled ->
                _darkModeEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            userPreferences.userNameFlow().collectLatest { name ->
                _userName.value = name
            }
        }
        viewModelScope.launch {
            userPreferences.userJobFlow().collectLatest { job ->
                _userJob.value = job
            }
        }
        viewModelScope.launch {
            userPreferences.userSalaryFlow().collectLatest { salary ->
                _userSalary.value = salary
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _darkModeEnabled.value = enabled
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
        }
    }

    fun updateProfile(name: String, job: String, salary: Long) {
        viewModelScope.launch {
            userPreferences.setUserName(name)
            userPreferences.setUserJob(job)
            userPreferences.setUserSalary(salary)
        }
    }
}
