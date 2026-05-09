package com.tonyyang.typtt.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.data.Articles
import com.tonyyang.typtt.data.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber

class BoardPagingSource(
    private val boardUrl: String
) : PagingSource<String, Articles>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Articles> {
        val url = params.key ?: boardUrl
        return try {
            val (articles, nextUrl) = withContext(Dispatchers.IO) { fetchArticles(url) }
            LoadResult.Page(
                data = articles,
                prevKey = null,
                nextKey = nextUrl
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to load articles from $url")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Articles>): String? = null

    private fun fetchArticles(url: String): Pair<List<Articles>, String?> {
        val cookies = Jsoup.connect(url)
            .method(Connection.Method.GET)
            .execute()
            .cookies()
            .apply { this["over18"] = "1" }
        val doc = Jsoup.connect(url)
            .data("from", url.removePrefix(BuildConfig.BASE_URL))
            .data("yes", "yes")
            .cookies(cookies)
            .post()
        return Pair(parseArticles(doc), parsePreviousPageUrl(doc))
    }

    private fun parseArticles(doc: Document): List<Articles> {
        val container = doc.selectFirst("div .r-list-container")
            ?.selectFirst(".action-bar-margin")
            ?.selectFirst(".bbs-screen")
        var isTopArea = false
        return container?.allElements?.flatMap { element ->
            mutableListOf<Articles>().apply {
                when (element.attr("class")) {
                    "r-list-sep" -> isTopArea = true
                    "r-ent" -> add(parseArticle(element, isTopArea))
                }
            }
        }.orEmpty()
    }

    private fun parseArticle(element: Element, isTopArea: Boolean): Articles {
        val title = element.selectFirst(".title")?.text().orEmpty()
        val author = element.selectFirst(".author")?.text().orEmpty()
        val like = element.selectFirst(".nrec")?.text().orEmpty()
        val mark = element.selectFirst(".mark")?.text().orEmpty()
        val date = element.selectFirst(".date")?.text().orEmpty()
        val url = BuildConfig.BASE_URL + element.selectFirst(".title a")?.attr("href")
        val articleType = if (isTopArea) Type.PINNED_ARTICLES else Type.ARTICLES
        return Articles(title, author, like, mark, date, url, articleType)
    }

    private fun parsePreviousPageUrl(doc: Document): String? {
        val href = doc.selectFirst("div .action-bar")
            ?.selectFirst(".btn-group-paging")?.select(".btn")?.get(1)?.attr("href")
        return if (href.isNullOrEmpty()) null else BuildConfig.BASE_URL + href
    }
}
