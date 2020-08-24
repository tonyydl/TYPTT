package com.tonyyang.typtt.ui.hotboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.databinding.ItemHotboardBinding
import com.tonyyang.typtt.model.HotBoard
import kotlinx.android.synthetic.main.item_hotboard.view.*

class HotBoardAdapter : RecyclerView.Adapter<HotBoardAdapter.HotBoardHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, hotBoard: HotBoard)
    }

    var listener: OnItemClickListener? = null

    private val hotBoardList = mutableListOf<HotBoard>()

    fun updateList(hotBoardList: List<HotBoard>) {
        this.hotBoardList.clear()
        this.hotBoardList.addAll(hotBoardList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotBoardHolder {
        val itemBinding = ItemHotboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotBoardHolder(itemBinding.root)
    }

    override fun onBindViewHolder(holder: HotBoardHolder, position: Int) {
        holder.bind(hotBoardList[position])
    }

    override fun getItemCount(): Int {
        return hotBoardList.size
    }

    inner class HotBoardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(hotBoard: HotBoard) {
            itemView.name.text = hotBoard.name
            itemView.title.text = hotBoard.title
            itemView.category.text = hotBoard.category
            itemView.popularity.show(hotBoard.popularity)
            itemView.setOnClickListener {
                listener?.onItemClick(itemView, hotBoard)
            }
        }
    }
}
