package com.example.tonyyang.tonyptt

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar
import org.jsoup.Jsoup
import java.util.ArrayList
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.board_item.view.*
import org.jsoup.nodes.Document
import java.io.IOException

class MainActivity : AppCompatActivity(), LoadingEffectSupport {
    companion object {
        @Suppress("unused")
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: CustomAdapter
    private val boardList = ArrayList<Board>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureToolbar()
        initListView()
        loadData()
    }

    private fun loadData() {
        Observable.create((ObservableOnSubscribe<Document> { emitter ->
            startLoadingbar()
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
                    boardList.add(Board(
                            it.selectFirst(".board-name").text(),
                            it.selectFirst(".board-title").text(),
                            it.selectFirst(".board-class").text(),
                            Integer.valueOf(it.selectFirst(".board-nuser").child(0).text()),
                            "https://www.ptt.cc/" + it.getElementsByAttribute("href")))
                }
            }
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            mAdapter.updateList(boardList)
            stopLoadingbar()
        }
    }

    private fun configureToolbar() {
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
    }

    private fun initListView() {
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = CustomAdapter()
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DisposableManager.clear()
    }

    private inner class CustomAdapter : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        private var boardList: List<Board> = ArrayList()

        fun updateList(boardList: List<Board>) {
            setBoardList(boardList)
            notifyDataSetChanged()
        }

        private fun setBoardList(boardList: List<Board>) {
            this.boardList = boardList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = boardList[position].name
            holder.title.text = boardList[position].title
            holder.category.text = boardList[position].category
            holder.popularity.show(boardList[position].popularity)
        }

        override fun getItemCount(): Int {
            return boardList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.name
            val title: TextView = itemView.title
            val category: TextView = itemView.category
            val popularity: PopularityView = itemView.popularity
        }
    }

    override fun startLoadingbar() {
        if (progressbar is SmoothProgressBar) {
            (progressbar as SmoothProgressBar).progressiveStart()
            progressbar.visibility = View.VISIBLE
        }
    }

    override fun stopLoadingbar() {
        if (progressbar is SmoothProgressBar) {
            (progressbar as SmoothProgressBar).progressiveStop()
            progressbar.visibility = View.GONE
        }
    }
}
