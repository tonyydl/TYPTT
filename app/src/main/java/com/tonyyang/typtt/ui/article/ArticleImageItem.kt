package com.tonyyang.typtt.ui.article

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.tonyyang.typtt.data.ArticleElement

@Composable
fun ArticleImageItem(block: ArticleElement.ImageBlock, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AsyncImage(
        model = block.url,
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(block.url)))
            }
    )
}
