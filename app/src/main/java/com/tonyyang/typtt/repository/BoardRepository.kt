package com.tonyyang.typtt.repository

import androidx.lifecycle.switchMap
import androidx.paging.Config
import androidx.paging.toLiveData
import com.tonyyang.typtt.data.Articles

private const val PER_PAGE_SIZE = 15
private const val PRE_FETCH_DISTANCE = 4

object BoardRepository {

    fun postOfArticles(boardUrl: String): Listing<Articles> {
        val sourceFactory = BoardDataSourceFactory(boardUrl)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = PER_PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PRE_FETCH_DISTANCE
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