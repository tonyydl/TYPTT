package com.tonyyang.typtt.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.ExecuteOnceObserver
import com.tonyyang.typtt.data.Articles
import com.tonyyang.typtt.data.Type
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class BoardDataSource(
    private val boardUrl: String
) : PageKeyedDataSource<String, Articles>() {

    val initialLoad by lazy {
        MutableLiveData<NetworkState>()
    }

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Articles>
    ) {
        initialLoad.postValue(NetworkState.LOADING)
        fetchArticles(boardUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(ExecuteOnceObserver(onExecuteOnceNext = {
                callback.onResult(it.first, null, it.second)
                initialLoad.postValue(NetworkState.LOADED)
            }, onExecuteOnceError = {
                initialLoad.postValue(NetworkState.error(it.message ?: "unknown err"))
            }))
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Articles>) {
        initialLoad.postValue(NetworkState.LOADING)
        val boardUrl = params.key
        fetchArticles(boardUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(ExecuteOnceObserver(onExecuteOnceNext = {
                callback.onResult(it.first, it.second)
                initialLoad.postValue(NetworkState.LOADED)
            }, onExecuteOnceError = {
                initialLoad.postValue(NetworkState.error(it.message ?: "unknown err"))
            }))
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Articles>) {
        // do nothing
    }

    private fun fetchArticles(boardUrl: String): Observable<Pair<List<Articles>, String>> {
        return Observable.fromCallable {
            val cookies = fetchCookiesWithOver18Confirmation(boardUrl)
            val doc = fetchDocumentWithCookies(boardUrl, cookies)
            doc
        }.map { doc ->
            val articles = parseArticles(doc)
            val prevUrl = parsePreviousPageUrl(doc)
            Pair(articles, prevUrl)
        }
    }

    private fun fetchCookiesWithOver18Confirmation(boardUrl: String): Map<String, String> {
        return Jsoup.connect(boardUrl)
            .method(Connection.Method.GET)
            .execute()
            .cookies()
            .apply { this["over18"] = "1" }
    }

    private fun fetchDocumentWithCookies(boardUrl: String, cookies: Map<String, String>): Document {
        return Jsoup.connect(boardUrl)
            .data("from", boardUrl.removePrefix(BuildConfig.BASE_URL))
            .data("yes", "yes")
            .cookies(cookies)
            .post()
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

    private fun parsePreviousPageUrl(doc: Document): String {
        return BuildConfig.BASE_URL + doc.selectFirst("div .action-bar")
            ?.selectFirst(".btn-group-paging")?.select(".btn")?.get(1)?.attr("href")
    }
}