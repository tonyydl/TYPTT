package com.example.tonyyang.tonyptt.ui.hotboard

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.tonyyang.tonyptt.*
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
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

    private lateinit var viewModel: HotBoardViewModel

    private var hotBoardActivity: HotBoardActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i("onCreate")
        try {
            hotBoardActivity = activity as HotBoardActivity
        } catch (e: ClassCastException) {
            /** The activity doesn't implement the listener **/
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Logger.i("onCreateView")
        return inflater.inflate(R.layout.hotboard_fragment, container, false)
    }

    private val customAdapter: HotBoardFragment.CustomAdapter by lazy { CustomAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Logger.i("onActivityCreated")
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = customAdapter
        }
        viewModel = ViewModelProviders.of(this).get(HotBoardViewModel::class.java)
        viewModel.getHotBoardListLiveData().nonNullObserve(this) {
            customAdapter.updateList(it)
        }
        loadData()
    }

    private fun <T> LiveData<T>.nonNullObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
        this.observe(owner, android.arch.lifecycle.Observer {
            it?.let(observer)
        })
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
        })).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).doOnNext { it ->
            val container = it.selectFirst("div .b-list-container").selectFirst(".action-bar-margin").selectFirst(".bbs-screen")
            val elements = container.select(".b-ent").select(".board")
            if (elements.size > 0) {
                boardList.clear()
                elements.forEach {
                    boardList.add(HotBoard(
                            it.selectFirst(".board-name").text(),
                            it.selectFirst(".board-title").text(),
                            it.selectFirst(".board-class").text(),
                            Integer.valueOf(it.selectFirst(".board-nuser").child(0).text()),
                            "https://www.ptt.cc/" + it.getElementsByAttribute("href")))
                }
            }
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            viewModel.updateHotBoardList(boardList)
            hotBoardActivity?.stopLoadingBar()
        }
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

    override fun onResume() {
        super.onResume()
        Logger.i("onResume")
    }
}
