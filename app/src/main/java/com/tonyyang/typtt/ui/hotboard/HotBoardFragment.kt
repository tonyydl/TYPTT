package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonyyang.typtt.R
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.databinding.FragmentHotboardBinding
import com.tonyyang.typtt.setupActionBar


class HotBoardFragment : Fragment() {

    private lateinit var binding: FragmentHotboardBinding

    private val viewModel by viewModels<HotBoardViewModel>()

    private val hotBoardAdapter by lazy {
        HotBoardAdapter(hotBoardItemClickListener)
    }

    private val hotBoardItemClickListener: (View, HotBoard) -> Unit = { view, hotBoard ->
        HotBoardFragmentDirections.actionHotBoardFragmentToBoardFragment(
            hotBoard.name,
            hotBoard.title,
            hotBoard.url
        ).let {
            view.findNavController().navigate(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        viewModel.hotBoardListLiveData.observe(viewLifecycleOwner) {
            hotBoardAdapter.submitList(it)
        }
        viewModel.isRefreshLiveData.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
        viewModel.loadData()
    }
}
