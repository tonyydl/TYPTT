package com.tonyyang.typtt.ui.hotboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.repository.HotBoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class HotBoardUiState(
    val boards: List<HotBoard> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HotBoardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HotBoardUiState())
    val uiState: StateFlow<HotBoardUiState> = _uiState.asStateFlow()

    fun loadData() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { HotBoardRepository.getHotBoards() }
                .onSuccess { boards ->
                    _uiState.update { it.copy(boards = boards, isRefreshing = false) }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load hot boards")
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = e.message) }
                }
        }
    }
}
