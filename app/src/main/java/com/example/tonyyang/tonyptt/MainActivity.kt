package com.example.tonyyang.tonyptt

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.board_item.view.*

class MainActivity : AppCompatActivity(), LoadingEffectSupport {
    companion object {
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
        val boardObservable = Observable.create(ObservableOnSubscribe<Element> { emitter ->
            val doc = Jsoup.connect("https://www.ptt.cc/bbs/hotboards.html").get()
            Log.d(TAG, doc.title())
            val container = doc.selectFirst("div .b-list-container").selectFirst(".action-bar-margin").selectFirst(".bbs-screen")
            val elements = container.select(".b-ent").select(".board")
            for (element in elements) {
                emitter.onNext(element)
            }
            emitter.onComplete()
        })

        boardList.clear()

        boardObservable
                .flatMap { element ->
                    val list = ArrayList<Board>()
                    val board = Board(
                            element.selectFirst(".board-name").text(),
                            element.selectFirst(".board-title").text(),
                            element.selectFirst(".board-class").text(),
                            Integer.valueOf(element.selectFirst(".board-nuser").child(0).text()),
                            "https://www.ptt.cc/" + element.getElementsByAttribute("href")
                    )
                    list.add(board)
                    Observable.fromIterable(list)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Board> {
                    override fun onSubscribe(d: Disposable) {
                        Log.d(TAG, "onSubscribe: $d")
                        DisposableManager.add(d)
                        startLoadingbar()
                    }

                    override fun onNext(board: Board) {
                        Log.d(TAG, "onNext: ${board.name}")
                        boardList.add(board)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(TAG, "onError: ${e.message}")
                    }

                    override fun onComplete() {
                        Log.d(TAG, "onComplete")
                        mAdapter.updateList(boardList)
                        stopLoadingbar()
                    }
                })
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
