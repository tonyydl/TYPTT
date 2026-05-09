package com.tonyyang.typtt.repository

import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.data.HotBoard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object HotBoardRepository {

    suspend fun getHotBoards(): List<HotBoard> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("https://www.ptt.cc/bbs/hotboards.html").get()
        val elements = doc.select("div.b-list-container .b-ent .board")
        elements.mapNotNull { element ->
            val name = element.selectFirst(".board-name")?.text().orEmpty()
            val title = element.selectFirst(".board-title")?.text().orEmpty()
            val classType = element.selectFirst(".board-class")?.text().orEmpty()
            val nuser = element.selectFirst(".board-nuser")?.text()?.toIntOrNull() ?: 0
            val href = element.attr("href").let { if (it.isNotEmpty()) BuildConfig.BASE_URL + it else "" }

            if (name.isNotEmpty() && title.isNotEmpty() && classType.isNotEmpty() && href.isNotEmpty()) {
                HotBoard(name, title, classType, nuser, href)
            } else null
        }
    }
}
