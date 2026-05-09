package com.tonyyang.typtt.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonyyang.typtt.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ArticleUiState(
    val cookies: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ArticleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    fun loadCookies(articleUrl: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { ArticleRepository.getArticleCookies(articleUrl) }
                .onSuccess { cookies ->
                    _uiState.update { it.copy(cookies = cookies, isLoading = false) }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load cookies for $articleUrl")
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
