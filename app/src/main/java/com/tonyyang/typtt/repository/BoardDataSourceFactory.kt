package com.tonyyang.typtt.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.tonyyang.typtt.model.Articles

class BoardDataSourceFactory(private val boardUrl: String) :
    DataSource.Factory<String, Articles>() {
    val sourceLiveData = MutableLiveData<BoardDataSource>()
    override fun create(): DataSource<String, Articles> {
        return BoardDataSource(boardUrl).also {
            sourceLiveData.postValue(it)
        }
    }
}