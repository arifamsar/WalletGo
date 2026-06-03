package com.moneylite.ui.screens.forgot_password

data class ForgotPasswordUiState(
    val phoneNumber: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)
