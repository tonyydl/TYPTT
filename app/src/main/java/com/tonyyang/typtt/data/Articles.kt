package com.tonyyang.typtt.data

enum class Type {
    NONE, ARTICLES, PINNED_ARTICLES
}

data class Articles(
    val title: String,
    val author: String,
    val like: String,
    val mark: String,
    val date: String,
    val url: String,
    val type: Type = Type.NONE
)