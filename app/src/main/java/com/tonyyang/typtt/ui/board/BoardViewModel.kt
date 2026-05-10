package com.tonyyang.typtt.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.tonyyang.typtt.data.Articles
import com.tonyyang.typtt.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModel @Inject constructor() : ViewModel() {

    private val boardUrl = MutableStateFlow<String?>(null)

    val articles: Flow<PagingData<Articles>> = boardUrl
        .filterNotNull()
        .flatMapLatest { url -> BoardRepository.postOfArticles(url) }
        .cachedIn(viewModelScope)

    fun loadData(url: String) {
        if (boardUrl.value == null) boardUrl.value = url
    }
}
