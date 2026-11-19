package com.se7team.caughtu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.se7team.caughtu.data.AuthRepository
import com.se7team.caughtu.domain.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
)

/**
 * State holder for the Login and the Register screen. Each screen gets its own instance
 * (scoped to its navigation entry), so their states never mix.
 * Navigation after login reacts to the session, not to this ViewModel.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }

    fun login() {
        val s = _uiState.value
        val emailError = AuthValidator.validateEmail(s.email)
        val passwordError = if (s.password.isEmpty()) "Password is required" else null
        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        if (emailError != null || passwordError != null) return
        submit { repository.login(s.email.trim(), s.password) }
    }

    fun register() {
        val s = _uiState.value
        val emailError = AuthValidator.validateEmail(s.email)
        val passwordError = AuthValidator.validatePassword(s.password)
        val confirmError = AuthValidator.validatePasswordConfirmation(s.password, s.confirmPassword)
        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, confirmPasswordError = confirmError) }
        if (emailError != null || passwordError != null || confirmError != null) return
        submit { repository.register(s.email.trim(), s.password) }
    }

    private fun submit(call: suspend () -> Result<Unit>) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            val error = call().exceptionOrNull()
            _uiState.update {
                if (error == null) AuthUiState()
                else it.copy(isLoading = false, generalError = error.message ?: "Something went wrong. Please try again")
            }
        }
    }
}
