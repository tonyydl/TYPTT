package com.tonyyang.typtt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.repository.BoardRepository

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

    val isRefreshLiveData by lazy {
        MutableLiveData<Boolean>()
    }

    fun loadData(boardUrl: String) {
        articleLiveData.value = boardUrl
    }
}