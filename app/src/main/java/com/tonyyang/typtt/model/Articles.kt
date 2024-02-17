package com.tonyyang.typtt.model

enum class Type {
    NONE, ARTICLES, PINNED_ARTICLES
}

open class Articles(
    open val title: String,
    open val author: String,
    open val like: String,
    open val mark: String,
    open val date: String,
    open val url: String
) {
    open val type: Type = Type.NONE
}