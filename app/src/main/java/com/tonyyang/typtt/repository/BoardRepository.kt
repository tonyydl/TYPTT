package com.tonyyang.typtt.repository

import androidx.lifecycle.switchMap
import androidx.paging.Config
import androidx.paging.toLiveData
import com.tonyyang.typtt.model.Articles

private const val PER_PAGE_SIZE = 15

object BoardRepository {

    fun postOfArticles(boardUrl: String): Listing<Articles> {
        val sourceFactory = BoardDataSourceFactory(boardUrl)
        val livePagedList = sourceFactory.toLiveData(
                config = Config(
                        pageSize = PER_PAGE_SIZE,
                        enablePlaceholders = false,
                        prefetchDistance = 4
                )
        )
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }
}