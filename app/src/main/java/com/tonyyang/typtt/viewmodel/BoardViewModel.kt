package com.tonyyang.typtt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.repository.BoardRepository
import com.tonyyang.typtt.repository.NetworkState

class BoardViewModel : ViewModel() {

    private val articleLiveData by lazy {
        MutableLiveData<String>()
    }

    private val repoResult = articleLiveData.map { BoardRepository.postOfArticles(it) }

    val articleListLiveData: LiveData<PagedList<Articles>> = repoResult.switchMap { it.pagedList }

    val refreshState: LiveData<NetworkState> = repoResult.switchMap {
        it.refreshState
    }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun loadData(boardUrl: String) {
        articleLiveData.value = boardUrl
    }
}