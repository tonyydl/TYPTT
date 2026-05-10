package com.tonyyang.typtt.ui.hotboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.ui.theme.Background
import com.tonyyang.typtt.ui.theme.Surface
import com.tonyyang.typtt.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotBoardScreen(
    viewModel: HotBoardViewModel,
    onItemClick: (HotBoard) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.loadData() },
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
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
    }
}
