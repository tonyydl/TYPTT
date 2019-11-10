package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tonyyang.typtt.R
import kotlinx.android.synthetic.main.hotboard_fragment.*


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
        viewModel.hotBoardListLiveData.observe(this, Observer {
            hotBoardAdapter.updateList(it)
        })
        viewModel.isRefreshLiveData.observe(this, Observer {
            if (it) {
                hotBoardActivity?.startLoadingBar()
            } else {
                hotBoardActivity?.stopLoadingBar()
            }
        })
        viewModel.loadData()
    }
}
