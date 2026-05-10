package com.tonyyang.typtt.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.tonyyang.typtt.data.Articles
import com.tonyyang.typtt.ui.theme.Background
import com.tonyyang.typtt.ui.theme.Primary
import com.tonyyang.typtt.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    viewModel: BoardViewModel,
    boardUrl: String,
    onItemClick: (Articles) -> Unit
) {
    LaunchedEffect(boardUrl) {
        viewModel.loadData(boardUrl)
    }

    val articleItems = viewModel.articles.collectAsLazyPagingItems()
    val isRefreshing = articleItems.loadState.refresh is LoadState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { articleItems.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                count = articleItems.itemCount,
                key = articleItems.itemKey { it.url.ifEmpty { it.title } }
            ) { index ->
                articleItems[index]?.let { article ->
                    BoardItem(articles = article, onItemClick = onItemClick)
                    HorizontalDivider(color = Surface, thickness = 0.5.dp)
                }
            }

            if (articleItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }
        }
    }
}
