package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.tonyyang.typtt.R
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.nonNullObserve
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.hotboard_fragment.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


class HotBoardFragment : Fragment() {

    companion object {
        fun newInstance() = HotBoardFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(HotBoardViewModel::class.java)
    }

    private val hotBoardAdapter by lazy {
        HotBoardAdapter()
    }

    private val compositeDisposable = CompositeDisposable()

    private var hotBoardActivity: HotBoardActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            hotBoardActivity = activity as HotBoardActivity
        } catch (e: ClassCastException) {
            /** The activity doesn't implement the listener **/
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.hotboard_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = hotBoardAdapter
        }
        viewModel.getHotBoardListLiveData().nonNullObserve(this) {
            hotBoardAdapter.updateList(it)
        }
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun loadData() {
        Observable.create(ObservableOnSubscribe<Document> { emitter ->
            hotBoardActivity?.startLoadingBar()
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
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            viewModel.updateHotBoardList(it)
            hotBoardActivity?.stopLoadingBar()
        }.addTo(compositeDisposable)
    }
}
