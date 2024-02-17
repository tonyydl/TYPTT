package com.tonyyang.typtt.ui.article

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor() : ViewModel() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    val cookiesLiveData by lazy {
        MutableLiveData<Map<String, String>>()
    }

    fun loadCookies(articleUrl: String) {
        ArticleRepository.getArticleCookies(articleUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                cookiesLiveData.value = it
            }.addTo(compositeDisposable)
    }
}