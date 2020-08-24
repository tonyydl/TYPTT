package com.tonyyang.typtt.ui.board

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.databinding.ItemBoardBinding
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.model.Type
import kotlinx.android.synthetic.main.item_board.view.*

class BoardAdapter : PagedListAdapter<Articles, BoardAdapter.BoardHolder>(BoardDiffUtil()) {

    interface OnItemClickListener {
        fun onItemClick(view: View, articles: Articles)
    }

    var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        val itemBinding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardHolder(itemBinding.root)
    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class BoardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(articles: Articles) {
            SpannableString(articles.like).apply {
                val likeCount = articles.like.toIntOrNull() ?: 0
                val color = when {
                    likeCount in 1..9 -> {
                        ForegroundColorSpan(Color.GREEN)
                    }
                    likeCount >= 10 -> {
                        ForegroundColorSpan(Color.YELLOW)
                    }
                    articles.like == "爆" -> {
                        ForegroundColorSpan(Color.RED)
                    }
                    else -> {
                        ForegroundColorSpan(Color.GRAY)
                    }
                }
                setSpan(color, 0, articles.like.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }.let {
                itemView.like.text = it
            }

            itemView.title.text = articles.title
            itemView.author.text = articles.author
            itemView.pinned.visibility = if (articles.type == Type.PINNED_ARTICLES) View.VISIBLE else View.INVISIBLE
            itemView.date.text = articles.date
            itemView.setOnClickListener {
                listener?.onItemClick(itemView, articles)
            }
        }
    }

    class BoardDiffUtil : DiffUtil.ItemCallback<Articles>() {
        override fun areItemsTheSame(oldItem: Articles, newItem: Articles): Boolean {
            return oldItem.url == oldItem.url
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Articles, newItem: Articles): Boolean {
            return oldItem == newItem
        }
    }
}