package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.TextPrimary
import com.tonyyang.typtt.ui.theme.TextSecondary

@Composable
fun ArticleHeaderItem(header: ArticleElement.Header, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (header.author.isNotEmpty()) {
            Text(text = "作者", color = TextSecondary, fontSize = 11.sp)
            Text(text = header.author, color = TextPrimary, fontSize = 14.sp)
        }
        if (header.title.isNotEmpty()) {
            Text(
                text = "標題", color = TextSecondary, fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(text = header.title, color = TextPrimary, fontSize = 14.sp)
        }
        if (header.date.isNotEmpty()) {
            Text(
                text = "時間", color = TextSecondary, fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(text = header.date, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
