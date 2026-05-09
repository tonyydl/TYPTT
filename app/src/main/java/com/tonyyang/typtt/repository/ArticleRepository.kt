package com.tonyyang.typtt.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

object ArticleRepository {

    suspend fun getArticleCookies(articleUrl: String): Map<String, String> =
        withContext(Dispatchers.IO) {
            Jsoup.connect(articleUrl)
                .method(Connection.Method.GET)
                .execute()
                .cookies()
                .apply { this["over18"] = "1" }
        }
}
