package com.tonyyang.typtt.repository

import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.model.GeneralArticles
import com.tonyyang.typtt.model.PinnedArticles
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

object BoardRepository {

    fun getArticles(boardUrl: String): Observable<List<Articles>> = Observable.create(ObservableOnSubscribe<Document> {
        try {
            val cookies = Jsoup.connect(boardUrl)
                    .method(Connection.Method.GET)
                    .execute()
                    .cookies()
            cookies["over18"] = "1"
            val doc = Jsoup.connect(boardUrl)
                    .data("from", boardUrl.removePrefix(BuildConfig.BASE_URL))
                    .data("yes", "yes")
                    .cookies(cookies)
                    .post()
            it.onNext(doc)
            it.onComplete()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }).map {
        val container = it.selectFirst("div .r-list-container")
                .selectFirst(".action-bar-margin")
                .selectFirst(".bbs-screen")
        mutableListOf<Articles>().apply {
            var isTopArea = false
            container.allElements.forEach { element ->
                val className = element.attr("class")
                if ("r-list-sep" == className) {
                    isTopArea = true
                    return@forEach
                }
                if ("r-ent" == className) {
                    val title = element.selectFirst(".title")?.text() ?: ""
                    val author = element.selectFirst(".author")?.text() ?: ""
                    val like = element.selectFirst(".nrec")?.text() ?: ""
                    val mark = element.selectFirst(".mark")?.text() ?: ""
                    val date = element.selectFirst(".date")?.text() ?: ""
                    val url = element.selectFirst(".title")?.selectFirst("a")?.attr("href") ?: ""
                    /**
                     * If a separation line between a general article and a top article is detected,
                     * then use [PinnedArticles] instead of [GeneralArticles]
                     */
                    if (isTopArea) {
                        // Add item by descending
                        this.add(0, PinnedArticles(title, author, like, mark, date, url))
                        return@forEach
                    }
                    // Add item by descending
                    this.add(0, GeneralArticles(title, author, like, mark, date, url))
                }
            }
        }
    }
}