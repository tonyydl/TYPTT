package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.TextSecondary

@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel,
    articleUrl: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(articleUrl) {
        viewModel.loadArticle(articleUrl)
    }

    when {
        uiState.isLoading -> Box(
            modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator()
        }

        uiState.errorMessage != null -> Box(
            modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        ) {
            Button(onClick = { viewModel.loadArticle(articleUrl) }) {
                Text("重試")
            }
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(
                items = uiState.elements,
                key = { index, _ -> index }
            ) { _, element ->
                when (element) {
                    is ArticleElement.Header ->
                        ArticleHeaderItem(element)
                    is ArticleElement.TextBlock ->
                        ArticleTextItem(element, modifier = Modifier.padding(vertical = 4.dp))
                    is ArticleElement.QuoteBlock ->
                        ArticleQuoteItem(element, modifier = Modifier.padding(vertical = 4.dp))
                    is ArticleElement.ImageBlock ->
                        ArticleImageItem(element, modifier = Modifier.padding(vertical = 4.dp))
                    is ArticleElement.Divider ->
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = TextSecondary.copy(alpha = 0.3f)
                        )
                    is ArticleElement.Push ->
                        ArticlePushItem(element)
                }
            }
        }
    }
}
