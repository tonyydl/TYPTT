package com.tonyyang.typtt.ui.board

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonyyang.typtt.data.Articles
import com.tonyyang.typtt.databinding.FragmentBoardBinding
import com.tonyyang.typtt.repository.NetworkState
import com.tonyyang.typtt.setupActionBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoardFragment : Fragment() {

    private lateinit var binding: FragmentBoardBinding

    private val viewModel by viewModels<BoardViewModel>()

    private val boardAdapter by lazy {
        BoardAdapter().also {
            it.clickListener = boardItemClickListener
        }
    }

    private val boardItemClickListener: (View, Articles) -> Unit = { view, articles ->
        BoardFragmentDirections.actionBoardFragmentToArticleFragment(articles.title, articles.url)
            .let {
                view.findNavController().navigate(it)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.run {
            val bundle = BoardFragmentArgs.fromBundle(this)
            if (activity is AppCompatActivity) {
                (activity as AppCompatActivity).setupActionBar {
                    title = bundle.name
                    subtitle = bundle.title
                    setHomeButtonEnabled(true)
                    setDisplayHomeAsUpEnabled(true)
                }
            }
            bundle.url
        }.orEmpty()
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = boardAdapter
        }
        viewModel.articleListLiveData.observe(viewLifecycleOwner) {
            boardAdapter.submitList(it)
        }
        viewModel.refreshState.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it == NetworkState.LOADING
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        viewModel.loadData(url)
    }
}