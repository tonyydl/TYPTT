package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonyyang.typtt.R
import com.tonyyang.typtt.databinding.FragmentHotboardBinding
import com.tonyyang.typtt.model.HotBoard
import com.tonyyang.typtt.setupActionBar
import com.tonyyang.typtt.viewmodel.HotBoardViewModel


class HotBoardFragment : Fragment() {

    private lateinit var binding: FragmentHotboardBinding

    private val viewModel by lazy {
        ViewModelProvider(this).get(HotBoardViewModel::class.java)
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
        binding = FragmentHotboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setupActionBar {
            title = getString(R.string.hot_board_name)
            subtitle = null
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = hotBoardAdapter
        }
        viewModel.hotBoardListLiveData.observe(viewLifecycleOwner, {
            hotBoardAdapter.updateList(it)
        })
        viewModel.isRefreshLiveData.observe(viewLifecycleOwner, {
            binding.swipeRefresh.isRefreshing = it
        })
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
        viewModel.loadData()
    }

    companion object {
        private val TAG = HotBoardFragment::class.java.simpleName
    }
}
