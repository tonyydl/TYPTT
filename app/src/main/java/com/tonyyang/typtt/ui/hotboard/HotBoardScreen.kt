package com.tonyyang.typtt.ui.hotboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.data.HotBoard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotBoardScreen(
    viewModel: HotBoardViewModel,
    onItemClick: (HotBoard) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (uiState.isRefreshing) pullToRefreshState.startRefresh()
        else pullToRefreshState.endRefresh()
    }

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing && !uiState.isRefreshing) {
            viewModel.loadData()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222831))
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.boards) { hotBoard ->
                HotBoardItem(
                    hotBoard = hotBoard,
                    onItemClick = onItemClick
                )
                HorizontalDivider(color = Color(0xFF393E46), thickness = 0.5.dp)
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color(0xFF393E46),
            contentColor = Color(0xFF00ADB5)
        )
    }
}
