package com.tonyyang.typtt.ui.hotboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.R
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.ui.theme.Background
import com.tonyyang.typtt.ui.theme.Primary
import com.tonyyang.typtt.ui.theme.Surface
import com.tonyyang.typtt.ui.theme.TextPrimary

@Composable
fun HotBoardItem(
    hotBoard: HotBoard,
    onItemClick: (HotBoard) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .clickable { onItemClick(hotBoard) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryLabel(
            category = hotBoard.category,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Title
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = hotBoard.name,
                color = Primary,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hotBoard.title,
                color = Primary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Popularity
        HotBoardPopularity(
            popularity = hotBoard.popularity,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
private fun CategoryLabel(category: String, modifier: Modifier = Modifier) {
    // CJK Unified Ideographs and common CJK blocks start at U+2E80.
    // If every character is below that threshold it is Latin/ASCII and should
    // be rotated 90° so it reads naturally in a vertical layout.
    val isLatin = category.all { it.code < 0x2E80 }

    if (isLatin) {
        Text(
            text = category,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(Constraints())
                layout(placeable.height, placeable.width) {
                    placeable.placeWithLayer(
                        x = -(placeable.width - placeable.height) / 2,
                        y = -(placeable.height - placeable.width) / 2
                    ) {
                        rotationZ = 90f
                    }
                }
            }
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            category.forEach { char ->
                Text(
                    text = char.toString(),
                    color = TextPrimary,
                    fontSize = 21.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HotBoardPopularity(
    popularity: Int,
    modifier: Modifier = Modifier
) {
    val maxNumber = 2000
    val minNumber = 1

    Box(
        modifier = modifier.sizeIn(minWidth = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            popularity >= maxNumber -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_popular),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            popularity in minNumber until maxNumber -> {
                Text(
                    text = popularity.toString(),
                    color = Surface,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HotBoardItemPreview() {
    HotBoardItem(
        hotBoard = HotBoard(
            name = "NBA",
            title = "◎[NBA] 2017-18總冠軍 金州勇士隊",
            category = "籃球",
            popularity = 99,
            url = ""
        ),
        onItemClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HotBoardItemPopularPreview() {
    HotBoardItem(
        hotBoard = HotBoard(
            name = "Gossiping",
            title = "◎[八卦] 這是個八卦版",
            category = "八卦",
            popularity = 2500,
            url = ""
        ),
        onItemClick = {}
    )
}
