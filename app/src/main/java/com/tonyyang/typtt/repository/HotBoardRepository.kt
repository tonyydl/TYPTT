package com.tonyyang.typtt.repository

import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.model.HotBoard
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

object HotBoardRepository {

    fun getHotBoards(): Observable<MutableList<HotBoard>> = Observable.create(ObservableOnSubscribe<Document> { emitter ->
        try {
            val doc = Jsoup.connect("https://www.ptt.cc/bbs/hotboards.html").get()
            emitter.onNext(doc)
            emitter.onComplete()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }).map {
        val container = it.selectFirst("div .b-list-container")
                ?.selectFirst(".action-bar-margin")
                ?.selectFirst(".bbs-screen")
        val elements = container?.select(".b-ent")?.select(".board")
        mutableListOf<HotBoard>().apply {
            if ((elements?.size ?:0) > 0) {
                elements?.forEach { element ->
                    this.add(HotBoard(
                            element.selectFirst(".board-name")?.text().orEmpty(),
                            element.selectFirst(".board-title")?.text().orEmpty(),
                            element.selectFirst(".board-class")?.text().orEmpty(),
                            element.selectFirst(".board-nuser")?.child(0)?.text()?.toInt() ?: 0,
                            BuildConfig.BASE_URL + element.attr("href")))
                }
            }
        }
    }
}