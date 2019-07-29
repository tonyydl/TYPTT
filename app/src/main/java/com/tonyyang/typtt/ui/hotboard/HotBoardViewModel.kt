package com.tonyyang.typtt.ui.hotboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HotBoardViewModel : ViewModel() {
    private val hotBoardListLiveData: MutableLiveData<List<HotBoard>> = MutableLiveData()

    fun updateHotBoardList(hotBoardList: List<HotBoard>) {
        hotBoardListLiveData.value = hotBoardList
    }

    fun getHotBoardListLiveData(): MutableLiveData<List<HotBoard>> {
        return hotBoardListLiveData
    }
}
