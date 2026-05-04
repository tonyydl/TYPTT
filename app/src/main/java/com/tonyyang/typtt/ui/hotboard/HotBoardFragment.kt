package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tonyyang.typtt.R
import com.tonyyang.typtt.setupActionBar


class HotBoardFragment : Fragment() {

    private val viewModel by viewModels<HotBoardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HotBoardScreen(
                        viewModel = viewModel,
                        onItemClick = { hotBoard ->
                            HotBoardFragmentDirections.actionHotBoardFragmentToBoardFragment(
                                hotBoard.name,
                                hotBoard.title,
                                hotBoard.url
                            ).let {
                                findNavController().navigate(it)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setupActionBar {
            title = getString(R.string.hot_board_name)
            subtitle = null
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }
        viewModel.loadData()
    }
}
