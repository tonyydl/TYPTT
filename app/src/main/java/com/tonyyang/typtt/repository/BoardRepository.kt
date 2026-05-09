package com.tonyyang.typtt.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tonyyang.typtt.data.Articles
import kotlinx.coroutines.flow.Flow

private const val PAGE_SIZE = 20
private const val PRE_FETCH_DISTANCE = 4

object BoardRepository {

    fun postOfArticles(boardUrl: String): Flow<PagingData<Articles>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PRE_FETCH_DISTANCE
            ),
            pagingSourceFactory = { BoardPagingSource(boardUrl) }
        ).flow
    }
}
