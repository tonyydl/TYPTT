package com.tonyyang.typtt.data

sealed class ArticleElement {
    data class Header(
        val author: String,
        val title: String,
        val board: String,
        val date: String
    ) : ArticleElement()

    data class TextBlock(val text: String) : ArticleElement()
    data class QuoteBlock(val text: String) : ArticleElement()
    data class ImageBlock(val url: String) : ArticleElement()
    object Divider : ArticleElement()

    data class Push(
        val type: PushType,
        val user: String,
        val content: String,
        val date: String
    ) : ArticleElement()
}

enum class PushType { PUSH, BOO, NEUTRAL }
