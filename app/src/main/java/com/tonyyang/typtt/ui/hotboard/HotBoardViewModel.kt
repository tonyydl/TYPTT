package com.tonyyang.typtt.ui.hotboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.repository.HotBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class HotBoardViewModel : ViewModel() {

    private val _hotBoardList = MutableStateFlow<List<HotBoard>>(emptyList())
    val hotBoardList: StateFlow<List<HotBoard>> = _hotBoardList.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching { HotBoardRepository.getHotBoards() }
                .onSuccess { _hotBoardList.value = it }
                .onFailure { Timber.e(it, "Failed to load hot boards") }
            _isRefreshing.value = false
        }
    }
}
