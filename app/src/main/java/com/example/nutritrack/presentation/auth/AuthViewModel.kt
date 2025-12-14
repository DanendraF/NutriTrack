package com.example.nutritrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val usernameError: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    // Firebase Auth will be injected here later
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val registerState = _registerState.asStateFlow()

    // Update fields
    fun updateEmail(email: String) {
        _authState.value = _authState.value.copy(email = email, emailError = null)
    }

    fun updatePassword(password: String) {
        _authState.value = _authState.value.copy(password = password, passwordError = null)
    }

    fun updateUsername(username: String) {
        _authState.value = _authState.value.copy(username = username, usernameError = null)
    }

    // Validation
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Username cannot be empty"
            username.length < 3 -> "Username must be at least 3 characters"
            else -> null
        }
    }

    // Login
    fun login() {
        val state = _authState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _authState.value = state.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            try {
                _loginState.value = UiState.Loading

                // TODO: Replace with Firebase Authentication
                // Simulate API call
                delay(1500)

                // For now, just return a mock user ID
                val userId = "temp_user_${System.currentTimeMillis()}"

                _loginState.value = UiState.Success(userId)
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Login failed")
            }
        }
    }

    // Register
    fun register() {
        val state = _authState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val usernameError = validateUsername(state.username)

        if (emailError != null || passwordError != null || usernameError != null) {
            _authState.value = state.copy(
                emailError = emailError,
                passwordError = passwordError,
                usernameError = usernameError
            )
            return
        }

        viewModelScope.launch {
            try {
                _registerState.value = UiState.Loading

                // TODO: Replace with Firebase Authentication
                // Simulate API call
                delay(1500)

                // For now, just return a mock user ID
                val userId = "temp_user_${System.currentTimeMillis()}"

                _registerState.value = UiState.Success(userId)
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // Reset states
    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = UiState.Idle
    }

    fun clearErrors() {
        _authState.value = _authState.value.copy(
            emailError = null,
            passwordError = null,
            usernameError = null
        )
    }
}
