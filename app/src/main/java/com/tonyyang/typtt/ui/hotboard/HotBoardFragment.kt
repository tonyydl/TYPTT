package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonyyang.typtt.R
import com.tonyyang.typtt.setupActionBar
import kotlinx.android.synthetic.main.fragment_hotboard.*


class HotBoardFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(HotBoardViewModel::class.java)
    }

    private val hotBoardAdapter by lazy {
        HotBoardAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_hotboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(false)
        }
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = hotBoardAdapter
        }
        viewModel.hotBoardListLiveData.observe(this, Observer {
            hotBoardAdapter.updateList(it)
        })
        viewModel.isRefreshLiveData.observe(this, Observer {
            swipe_refresh.isRefreshing = it
        })
        swipe_refresh.setOnRefreshListener {
            viewModel.loadData()
        }
        viewModel.loadData()
    }
}
