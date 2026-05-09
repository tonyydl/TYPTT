package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.TextPrimary

@Composable
fun ArticleTextItem(block: ArticleElement.TextBlock, modifier: Modifier = Modifier) {
    SelectionContainer(modifier = modifier) {
        Text(
            text = block.text,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}
