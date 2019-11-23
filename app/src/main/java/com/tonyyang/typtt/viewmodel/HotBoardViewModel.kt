package com.tonyyang.typtt.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.model.HotBoard
import com.tonyyang.typtt.repository.HotBoardRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HotBoardViewModel : ViewModel() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    val hotBoardListLiveData by lazy {
        MutableLiveData<List<HotBoard>>()
    }

    val isRefreshLiveData by lazy {
        MutableLiveData<Boolean>()
    }

    fun loadData() {
        isRefreshLiveData.postValue(true)
        HotBoardRepository.getHotBoards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hotBoardListLiveData.value = it
                    isRefreshLiveData.value = false
                }.addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
