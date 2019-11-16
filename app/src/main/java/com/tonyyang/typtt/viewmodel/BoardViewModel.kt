package com.tonyyang.typtt.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.repository.BoardRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class BoardViewModel : ViewModel() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    val articleListLiveData by lazy {
        MutableLiveData<List<Articles>>()
    }

    val isRefreshLiveData by lazy {
        MutableLiveData<Boolean>()
    }

    fun loadData(boardUrl: String) {
        isRefreshLiveData.postValue(true)
        BoardRepository.getArticles(boardUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    articleListLiveData.postValue(it)
                    isRefreshLiveData.postValue(false)
                }.addTo(compositeDisposable)
    }
}