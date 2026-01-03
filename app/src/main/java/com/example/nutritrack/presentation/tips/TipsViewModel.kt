package com.example.nutritrack.presentation.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.TipsRepository
import com.example.nutritrack.data.repository.TipsDataInitializer
import com.example.nutritrack.domain.model.Tip
import com.example.nutritrack.domain.model.DailyRecommend
import com.example.nutritrack.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TipsViewModel(
    private val tipsRepository: TipsRepository,
    private val tipsDataInitializer: TipsDataInitializer
) : ViewModel() {

    private val _tips = MutableStateFlow<List<Tip>>(emptyList())
    val tips: StateFlow<List<Tip>> = _tips.asStateFlow()

    private val _dailyRecommends = MutableStateFlow<List<DailyRecommend>>(emptyList())
    val dailyRecommends: StateFlow<List<DailyRecommend>> = _dailyRecommends.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Initialize data first, then load
        viewModelScope.launch {
            tipsDataInitializer.initializeDataIfNeeded()
            loadTips()
            loadDailyRecommends()
            loadArticles()
        }
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

    private fun loadDailyRecommends() {
        viewModelScope.launch {
            try {
                tipsRepository.getDailyRecommends().collect { recommendsList ->
                    _dailyRecommends.value = recommendsList
                }
            } catch (e: Exception) {
                android.util.Log.e("TipsViewModel", "Error loading daily recommends", e)
            }
        }
    }

    private fun loadArticles() {
        viewModelScope.launch {
            try {
                tipsRepository.getArticles().collect { articlesList ->
                    _articles.value = articlesList
                }
            } catch (e: Exception) {
                android.util.Log.e("TipsViewModel", "Error loading articles", e)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
