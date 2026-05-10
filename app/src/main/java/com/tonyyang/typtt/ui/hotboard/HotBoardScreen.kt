package com.tonyyang.typtt.ui.hotboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.ui.theme.Background
import com.tonyyang.typtt.ui.theme.Primary
import com.tonyyang.typtt.ui.theme.Surface
import com.tonyyang.typtt.ui.theme.TextSecondary

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
            .background(Background)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        if (uiState.errorMessage != null && uiState.boards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = uiState.errorMessage ?: "", color = TextSecondary)
                Button(
                    onClick = { viewModel.loadData() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("重試")
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.boards, key = { it.name }) { hotBoard ->
                    HotBoardItem(
                        hotBoard = hotBoard,
                        onItemClick = onItemClick
                    )
                    HorizontalDivider(color = Surface, thickness = 0.5.dp)
                }
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Surface,
            contentColor = Primary
        )
    }
}
