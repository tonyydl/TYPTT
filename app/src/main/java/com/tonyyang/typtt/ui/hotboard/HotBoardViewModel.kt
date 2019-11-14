package com.tonyyang.typtt.ui.hotboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.repository.HotBoardRepository
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
                .observeOn(Schedulers.io())
                .subscribe {
                    hotBoardListLiveData.postValue(it)
                    isRefreshLiveData.postValue(false)
                }.addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
