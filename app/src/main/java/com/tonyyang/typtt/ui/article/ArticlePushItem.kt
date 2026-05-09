package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.data.PushType
import com.tonyyang.typtt.ui.theme.TextPrimary
import com.tonyyang.typtt.ui.theme.TextSecondary

private val PushBlue = Color(0xFF4A9EFF)
private val BooRed = Color(0xFFFF4444)
private val NeutralGray = Color(0xFF888888)

@Composable
fun ArticlePushItem(push: ArticleElement.Push, modifier: Modifier = Modifier) {
    val (tagText, tagColor) = when (push.type) {
        PushType.PUSH -> "推" to PushBlue
        PushType.BOO -> "噓" to BooRed
        PushType.NEUTRAL -> "→" to NeutralGray
    }
    Row(modifier = modifier.padding(vertical = 2.dp)) {
        Text(text = tagText, color = tagColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = push.user, color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = push.content, color = TextPrimary, fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(text = push.date, color = TextSecondary, fontSize = 11.sp)
    }
}
