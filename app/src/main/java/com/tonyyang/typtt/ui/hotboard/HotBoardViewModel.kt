package com.tonyyang.typtt.ui.hotboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tonyyang.typtt.addTo
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class HotBoardViewModel : ViewModel() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    val hotBoardListLiveData by lazy {
        MutableLiveData<List<HotBoard>>()
    }

    val isRefreshLiveData by lazy {
        MutableLiveData<Boolean>()
    }

    fun loadData() {
        Observable.create(ObservableOnSubscribe<Document> { emitter ->
            isRefreshLiveData.postValue(true)
            try {
                val doc = Jsoup.connect("https://www.ptt.cc/bbs/hotboards.html").get()
                emitter.onNext(doc)
                emitter.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).subscribeOn(Schedulers.io()).map { t ->
            val container = t.selectFirst("div .b-list-container")
                    .selectFirst(".action-bar-margin")
                    .selectFirst(".bbs-screen")
            val elements = container.select(".b-ent").select(".board")
            mutableListOf<HotBoard>().also {
                if (elements.size > 0) {
                    elements.forEach { element ->
                        it.add(HotBoard(
                                element.selectFirst(".board-name").text(),
                                element.selectFirst(".board-title").text(),
                                element.selectFirst(".board-class").text(),
                                Integer.valueOf(element.selectFirst(".board-nuser").child(0).text()),
                                "https://www.ptt.cc/" + element.getElementsByAttribute("href")))
                    }
                }
            }
        }.observeOn(Schedulers.io()).subscribe {
            hotBoardListLiveData.postValue(it)
            isRefreshLiveData.postValue(false)
        }.addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
