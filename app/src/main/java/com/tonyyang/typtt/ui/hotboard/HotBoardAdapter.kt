package com.tonyyang.typtt.ui.hotboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.R
import kotlinx.android.synthetic.main.board_item.view.*

class HotBoardAdapter : RecyclerView.Adapter<HotBoardAdapter.HotBoardHolder>() {

    private val boardList = ArrayList<HotBoard>()

    fun updateList(hotBoardList: List<HotBoard>) {
        this.boardList.clear()
        this.boardList.addAll(hotBoardList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotBoardHolder {
        return HotBoardHolder(LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false))
    }

    override fun onBindViewHolder(holder: HotBoardHolder, position: Int) {
        holder.bind(boardList[position])
    }

    override fun getItemCount(): Int {
        return boardList.size
    }

    class HotBoardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(hotBoard: HotBoard) {
            itemView.name.text = hotBoard.name
            itemView.title.text = hotBoard.title
            itemView.category.text = hotBoard.category
            itemView.popularity.show(hotBoard.popularity)
        }
    }
}
