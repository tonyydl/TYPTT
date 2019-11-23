package com.tonyyang.typtt.model

data class GeneralArticles(override val title: String,
                           override val author: String,
                           override val like: String,
                           override val mark: String,
                           override val date: String,
                           override val url: String) : Articles(title, author, like, mark, date, url) {
    override val type: Type
        get() = Type.ARTICLES
}