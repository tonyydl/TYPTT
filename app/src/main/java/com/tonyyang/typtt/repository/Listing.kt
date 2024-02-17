package com.tonyyang.typtt.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class Listing<T : Any>(
    val pagedList: LiveData<PagedList<T>>,
    val refresh: () -> Unit,
    val refreshState: LiveData<NetworkState>
)