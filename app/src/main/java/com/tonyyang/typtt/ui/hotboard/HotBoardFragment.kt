package com.tonyyang.typtt.ui.hotboard

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
import com.tonyyang.typtt.model.HotBoard
import com.tonyyang.typtt.setupActionBar
import com.tonyyang.typtt.viewmodel.HotBoardViewModel
import kotlinx.android.synthetic.main.fragment_hotboard.*


class HotBoardFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(HotBoardViewModel::class.java)
    }

    private val hotBoardAdapter by lazy {
        HotBoardAdapter().also {
            it.listener = hotBoardItemListener
        }
    }

    private val hotBoardItemListener = object : HotBoardAdapter.OnItemClickListener {

        override fun onItemClick(view: View, hotBoard: HotBoard) {
            Log.d(TAG, "onItemClick, view: $view, hotBoard: $hotBoard")
            HotBoardFragmentDirections.actionHotBoardFragmentToBoardFragment(
                    hotBoard.name,
                    hotBoard.title,
                    hotBoard.url)
                    .let {
                        view.findNavController().navigate(it)
                    }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_hotboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setupActionBar {
                title = getString(R.string.hot_board_name)
                subtitle = null
                setHomeButtonEnabled(false)
                setDisplayHomeAsUpEnabled(false)
            }
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

    companion object {
        private val TAG = HotBoardFragment::class.java.simpleName
    }
}
