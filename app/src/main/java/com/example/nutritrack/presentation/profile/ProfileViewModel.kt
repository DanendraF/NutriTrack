package com.example.nutritrack.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.AuthRepository
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState = _logoutState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            userRepository.getCurrentUser()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load profile"
                        )
                    }
                }
                .collect { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading

            try {
                authRepository.logout()
                _logoutState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _logoutState.value = UiState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = UiState.Idle
    }
}
