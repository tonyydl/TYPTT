package com.tonyyang.typtt.repository

import androidx.paging.DataSource
import com.tonyyang.typtt.model.Articles

class BoardDataSourceFactory(private val boardUrl: String) : DataSource.Factory<String, Articles>() {
    override fun create(): DataSource<String, Articles> {
        return BoardDataSource(boardUrl)
    }
}