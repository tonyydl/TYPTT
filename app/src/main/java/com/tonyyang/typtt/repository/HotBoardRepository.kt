package com.tonyyang.typtt.repository

import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.data.HotBoard
import io.reactivex.rxjava3.core.Observable
import org.jsoup.Jsoup
import java.io.IOException

object HotBoardRepository {

    fun getHotBoards(): Observable<List<HotBoard>> =
        Observable.create { emitter ->
            try {
                val doc = Jsoup.connect("https://www.ptt.cc/bbs/hotboards.html").get()
                emitter.onNext(doc)
                emitter.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.map { doc ->
            val elements = doc.select("div.b-list-container .b-ent .board")
            elements.mapNotNull { element ->
                val name = element.selectFirst(".board-name")?.text().orEmpty()
                val title = element.selectFirst(".board-title")?.text().orEmpty()
                val classType = element.selectFirst(".board-class")?.text().orEmpty()
                val nuser = element.selectFirst(".board-nuser")?.child(0)?.text()?.toIntOrNull() ?: 0
                val href = element.attr("href").let { if (it.isNotEmpty()) BuildConfig.BASE_URL + it else "" }

                if (name.isNotEmpty() && title.isNotEmpty() && classType.isNotEmpty() && href.isNotEmpty()) {
                    HotBoard(name, title, classType, nuser, href)
                } else null
            }.toMutableList()
        }
}