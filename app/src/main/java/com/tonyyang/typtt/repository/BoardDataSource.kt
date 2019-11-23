package com.tonyyang.typtt.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.ExecuteOnceObserver
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.model.GeneralArticles
import com.tonyyang.typtt.model.PinnedArticles
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class BoardDataSource(private val boardUrl: String) : PageKeyedDataSource<String, Articles>() {

    val initialLoad by lazy {
        MutableLiveData<NetworkState>()
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Articles>) {
        initialLoad.postValue(NetworkState.LOADING)
        getSources(boardUrl)
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
        getSources(boardUrl)
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

    private fun getSources(boardUrl: String): Observable<Pair<MutableList<Articles>, String>> {
        return Observable.create(ObservableOnSubscribe<Document> {
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
            val prevUrl = BuildConfig.BASE_URL + it.selectFirst("div .action-bar").selectFirst(".btn-group-paging").select(".btn")[1].attr("href")
            Pair(mutableListOf<Articles>().apply {
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
                        val url = BuildConfig.BASE_URL + element.selectFirst(".title")?.selectFirst("a")?.attr("href")
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
            }, prevUrl)
        }
    }
}