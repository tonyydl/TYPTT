package com.tonyyang.typtt.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class Listing<T>(
        val pagedList: LiveData<PagedList<T>>,
        val refresh: () -> Unit,
        val refreshState: LiveData<NetworkState>
)