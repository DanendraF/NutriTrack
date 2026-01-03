package com.example.nutritrack.presentation.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.TipsRepository
import com.example.nutritrack.domain.model.Tip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TipsViewModel(
    private val tipsRepository: TipsRepository
) : ViewModel() {

    private val _tips = MutableStateFlow<List<Tip>>(emptyList())
    val tips: StateFlow<List<Tip>> = _tips.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTips()
    }

    private fun loadTips() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                tipsRepository.getTips().collect { tipsList ->
                    _tips.value = tipsList
                    _isLoading.value = false
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
