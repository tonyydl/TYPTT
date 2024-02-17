package com.tonyyang.typtt.ui.hotboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.data.HotBoard
import com.tonyyang.typtt.databinding.ItemHotboardBinding

class HotBoardAdapter(
    private val clickListener: (View, HotBoard) -> Unit
) : ListAdapter<HotBoard, HotBoardAdapter.HotBoardHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotBoardHolder {
        return HotBoardHolder(
            ItemHotboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            clickListener
        )
    }

    override fun onBindViewHolder(holder: HotBoardHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HotBoardHolder(
        binding: ItemHotboardBinding,
        private val clickListener: (View, HotBoard) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val nameTv = binding.name
        private val titleTv = binding.title
        private val categoryTv = binding.category
        private val popularityTv = binding.popularity

        fun bind(hotBoard: HotBoard) {
            nameTv.text = hotBoard.name
            titleTv.text = hotBoard.title
            categoryTv.text = hotBoard.category
            popularityTv.show(hotBoard.popularity)
            itemView.setOnClickListener {
                clickListener.invoke(itemView, hotBoard)
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<HotBoard>() {
            override fun areItemsTheSame(oldItem: HotBoard, newItem: HotBoard): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: HotBoard, newItem: HotBoard): Boolean {
                return oldItem == newItem
            }
        }
    }
}
