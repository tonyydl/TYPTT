package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.Primary
import com.tonyyang.typtt.ui.theme.TextSecondary

@Composable
fun ArticleQuoteItem(block: ArticleElement.QuoteBlock, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Primary.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Primary)
        )
        Text(
            text = block.text,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
