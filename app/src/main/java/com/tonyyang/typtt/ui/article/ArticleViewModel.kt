package com.tonyyang.typtt.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonyyang.typtt.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor() : ViewModel() {

    private val _cookies = MutableStateFlow<Map<String, String>>(emptyMap())
    val cookies: StateFlow<Map<String, String>> = _cookies.asStateFlow()

    fun loadCookies(articleUrl: String) {
        viewModelScope.launch {
            runCatching { ArticleRepository.getArticleCookies(articleUrl) }
                .onSuccess { _cookies.value = it }
                .onFailure { Timber.e(it, "Failed to load cookies for $articleUrl") }
        }
    }
}
