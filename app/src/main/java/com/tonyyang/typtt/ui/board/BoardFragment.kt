package com.tonyyang.typtt.ui.board

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonyyang.typtt.R
import com.tonyyang.typtt.expandActionBar
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.repository.NetworkState
import com.tonyyang.typtt.viewmodel.BoardViewModel
import kotlinx.android.synthetic.main.fragment_board.*

class BoardFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(BoardViewModel::class.java)
    }

    private val boardAdapter by lazy {
        BoardAdapter().also {
            it.listener = boardItemListener
        }
    }

    private val boardItemListener = object : BoardAdapter.OnItemClickListener {

        override fun onItemClick(view: View, articles: Articles) {
            Log.d(TAG, "onItemClick, view: $view, articles: $articles")
            BoardFragmentDirections.actionBoardFragmentToArticleFragment(articles.title, articles.url).let {
                view.findNavController().navigate(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.run {
            val bundle = BoardFragmentArgs.fromBundle(this)
            val act = (activity as AppCompatActivity)
            act.expandActionBar {
                title = bundle.name
                subtitle = bundle.title
            }
            bundle.url
        } ?: ""
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = boardAdapter
        }
        viewModel.articleListLiveData.observe(this, Observer {
            boardAdapter.submitList(it)
        })
        viewModel.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            viewModel.refresh()
        }
        viewModel.loadData(url)
    }

    companion object {
        private val TAG = BoardFragment::class.java.simpleName
    }
}