package com.tonyyang.typtt.repository

import io.reactivex.Observable
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException

object ArticleRepository {

    fun getArticleCookies(articleUrl: String): Observable<Map<String, String>> = Observable.create {
        try {
            val cookies = Jsoup.connect(articleUrl)
                    .method(Connection.Method.GET)
                    .execute()
                    .cookies()
            cookies["over18"] = "1"
            it.onNext(cookies)
            it.onComplete()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}