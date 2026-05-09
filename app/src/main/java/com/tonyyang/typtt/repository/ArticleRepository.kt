package com.tonyyang.typtt.repository

import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.data.ArticleElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

object ArticleRepository {

    suspend fun getArticleContent(articleUrl: String): List<ArticleElement> =
        withContext(Dispatchers.IO) {
            val cookies = Jsoup.connect(articleUrl)
                .method(Connection.Method.GET)
                .execute()
                .cookies()
                .apply { this["over18"] = "1" }

            val html = Jsoup.connect(articleUrl)
                .data("from", articleUrl.removePrefix(BuildConfig.BASE_URL))
                .data("yes", "yes")
                .cookies(cookies)
                .post()
                .outerHtml()

            ArticleParser.parse(html)
        }
}
