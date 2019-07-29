package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.R
import com.tonyyang.typtt.addTo
import com.tonyyang.typtt.nonNullObserve
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.board_item.view.*
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

    private val customAdapter by lazy {
        CustomAdapter()
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
            adapter = customAdapter
        }
        viewModel.getHotBoardListLiveData().nonNullObserve(this) {
            customAdapter.updateList(it)
        }
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun loadData() {
        val boardList = ArrayList<HotBoard>()
        Observable.create((ObservableOnSubscribe<Document> { emitter ->
            hotBoardActivity?.startLoadingBar()
            try {
                val doc = Jsoup.connect("https://www.ptt.cc/bbs/hotboards.html").get()
                emitter.onNext(doc)
                emitter.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).doOnNext {
            val container = it.selectFirst("div .b-list-container").selectFirst(".action-bar-margin").selectFirst(".bbs-screen")
            val elements = container.select(".b-ent").select(".board")
            if (elements.size > 0) {
                boardList.clear()
                elements.forEach { element ->
                    boardList.add(HotBoard(
                            element.selectFirst(".board-name").text(),
                            element.selectFirst(".board-title").text(),
                            element.selectFirst(".board-class").text(),
                            Integer.valueOf(element.selectFirst(".board-nuser").child(0).text()),
                            "https://www.ptt.cc/" + element.getElementsByAttribute("href")))
                }
            }
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            viewModel.updateHotBoardList(boardList)
            hotBoardActivity?.stopLoadingBar()
        }.addTo(compositeDisposable)
    }

    private inner class CustomAdapter : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        private val boardList = ArrayList<HotBoard>()

        fun updateList(hotBoardList: List<HotBoard>) {
            this.boardList.clear()
            this.boardList.addAll(hotBoardList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = boardList[position].name
            holder.title.text = boardList[position].title
            holder.category.text = boardList[position].category
            holder.hotBoardPopularity.show(boardList[position].popularity)
        }

        override fun getItemCount(): Int {
            return boardList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.name
            val title: TextView = itemView.title
            val category: TextView = itemView.category
            val hotBoardPopularity: HotBoardPopularityView = itemView.popularity
        }
    }
}
