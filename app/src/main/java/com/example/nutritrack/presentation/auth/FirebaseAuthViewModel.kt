package com.example.nutritrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.local.preferences.AuthPreferences
import com.example.nutritrack.data.repository.AuthRepository
import com.example.nutritrack.data.repository.AuthResult
import com.example.nutritrack.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseAuthViewModel(
    private val authRepository: AuthRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val registerState = _registerState.asStateFlow()

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        // Check both Firebase and SharedPreferences for persistent login
        if (authRepository.isUserLoggedIn() && authPreferences.isLoggedIn()) {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId != null) {
                _loginState.value = UiState.Success(userId)
            }
        } else if (authPreferences.isLoggedIn()) {
            // SharedPreferences says logged in but Firebase doesn't - clear prefs
            authPreferences.clearLoginState()
        }
    }

    fun updateEmail(email: String) {
        _authState.value = _authState.value.copy(email = email, emailError = null)
    }

    fun updatePassword(password: String) {
        _authState.value = _authState.value.copy(password = password, passwordError = null)
    }

    fun updateUsername(username: String) {
        _authState.value = _authState.value.copy(username = username, usernameError = null)
    }

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
            _loginState.value = UiState.Loading

            when (val result = authRepository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    // Save login state to SharedPreferences for persistent login
                    authPreferences.saveLoginState(result.userId, state.email)
                    _loginState.value = UiState.Success(result.userId)
                }
                is AuthResult.Error -> {
                    _loginState.value = UiState.Error(result.message)
                }
            }
        }
    }

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
            _registerState.value = UiState.Loading

            when (val result = authRepository.register(
                email = state.email,
                password = state.password,
                username = state.username
            )) {
                is AuthResult.Success -> {
                    // Save login state to SharedPreferences for persistent login
                    authPreferences.saveLoginState(result.userId, state.email)
                    _registerState.value = UiState.Success(result.userId)
                }
                is AuthResult.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Clear login state from SharedPreferences
            authPreferences.clearLoginState()
            authRepository.logout()
            _loginState.value = UiState.Idle
            _registerState.value = UiState.Idle
            _authState.value = AuthState()
        }
    }

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

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUser()?.uid
    }
}
