package com.tonyyang.typtt.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.tonyyang.typtt.data.Articles
import com.tonyyang.typtt.data.Type
import com.tonyyang.typtt.ui.theme.Background
import com.tonyyang.typtt.ui.theme.Pinned
import com.tonyyang.typtt.ui.theme.TextPrimary
import com.tonyyang.typtt.ui.theme.TextSecondary

private fun likeColor(like: String): Color {
    val count = like.toIntOrNull() ?: 0
    return when {
        like == "爆" -> Color.Red
        count >= 10 -> Color.Yellow
        count in 1..9 -> Color.Green
        else -> Color.Gray
    }
}

@Composable
fun BoardItem(
    articles: Articles,
    onItemClick: (Articles) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .clickable { onItemClick(articles) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = articles.like,
            color = likeColor(articles.like),
            fontSize = 14.sp,
            modifier = Modifier.width(36.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (articles.type == Type.PINNED_ARTICLES) {
                    Text(
                        text = "★",
                        color = Pinned,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = articles.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = articles.author,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = articles.date,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoardItemPreview() {
    BoardItem(
        articles = Articles(
            title = "[問卦] 請問TYPTT各位覺得使用體驗如何呢？如果要打分數會打幾分呢？",
            author = "tonyyang",
            like = "爆",
            mark = "",
            date = "11/16",
            url = "",
            type = Type.ARTICLES
        ),
        onItemClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun BoardItemPinnedPreview() {
    BoardItem(
        articles = Articles(
            title = "[置頂] 批踢踢實業坊使用規則",
            author = "admin",
            like = "99",
            mark = "",
            date = "01/01",
            url = "",
            type = Type.PINNED_ARTICLES
        ),
        onItemClick = {}
    )
}
