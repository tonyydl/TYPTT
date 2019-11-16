package com.tonyyang.typtt.ui.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.R
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.model.Type
import kotlinx.android.synthetic.main.item_board.view.*

class BoardAdapter : RecyclerView.Adapter<BoardAdapter.BoardHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, articles: Articles)
    }

    var listener: OnItemClickListener? = null

    fun updateList(articleList: List<Articles>) {
        this.articleList.apply {
            clear()
            addAll(articleList)
        }
        notifyDataSetChanged()
    }

    private val articleList by lazy {
        mutableListOf<Articles>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        return BoardHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_board, parent, false))
    }

    override fun getItemCount() = articleList.size

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
        holder.bind(articleList[position])
    }

    inner class BoardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(articles: Articles) {
            itemView.like.text = articles.like
            itemView.title.text = articles.title
            itemView.author.text = articles.author
            itemView.pinned.visibility = if (articles.type == Type.PINNED_ARTICLES) View.VISIBLE else View.INVISIBLE
            itemView.date.text = articles.date
        }
    }
}