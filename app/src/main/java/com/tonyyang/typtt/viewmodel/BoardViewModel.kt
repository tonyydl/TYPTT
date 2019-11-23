package com.tonyyang.typtt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.repository.BoardRepository
import com.tonyyang.typtt.repository.NetworkState

class BoardViewModel : ViewModel() {

    private val articleLiveData by lazy {
        MutableLiveData<String>()
    }

    private val repoResult = map(articleLiveData) {
        BoardRepository.postOfArticles(it)
    }

    val articleListLiveData: LiveData<PagedList<Articles>> = switchMap(repoResult) {
        it.pagedList
    }

    val refreshState: LiveData<NetworkState> = switchMap(repoResult) {
        it.refreshState
    }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun loadData(boardUrl: String) {
        articleLiveData.value = boardUrl
    }
}