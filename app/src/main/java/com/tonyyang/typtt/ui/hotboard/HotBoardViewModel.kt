package com.tonyyang.typtt.ui.hotboard

import androidx.lifecycle.ViewModel
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.repository.HotBoardRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class HotBoardViewModel : ViewModel() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val _hotBoardList = MutableStateFlow<List<HotBoard>>(emptyList())
    val hotBoardList: StateFlow<List<HotBoard>> = _hotBoardList.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadData() {
        _isRefreshing.value = true
        HotBoardRepository.getHotBoards()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _hotBoardList.value = it
                _isRefreshing.value = false
            }, {
                Timber.e(it, "Failed to load hot boards")
                _isRefreshing.value = false
            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
