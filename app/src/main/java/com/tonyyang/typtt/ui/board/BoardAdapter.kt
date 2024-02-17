package com.tonyyang.typtt.ui.board

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tonyyang.typtt.databinding.ItemBoardBinding
import com.tonyyang.typtt.model.Articles
import com.tonyyang.typtt.model.Type
import timber.log.Timber

class BoardAdapter :
    PagedListAdapter<Articles, BoardAdapter.BoardHolder>(diffCallback) {

    internal var clickListener: (View, Articles) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        return BoardHolder(
            ItemBoardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            clickListener
        )
    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        } ?: run {
            Timber.w("couldn't get item from position=$position")
        }
    }

    class BoardHolder(
        binding: ItemBoardBinding,
        private val listener: (View, Articles) -> Unit = { _, _ -> }
    ) : RecyclerView.ViewHolder(binding.root) {
        private val likeTv: TextView = binding.tvLike
        private val titleTv: TextView = binding.tvTitle
        private val authorTv: TextView = binding.tvAuthor
        private val pinnedTv: TextView = binding.tvPinned
        private val dateTv: TextView = binding.tvDate

        fun bind(articles: Articles) {
            likeTv.text = SpannableString(articles.like).apply {
                val likeCount = articles.like.toIntOrNull() ?: 0
                val color = when {
                    likeCount in level1 -> {
                        ForegroundColorSpan(Color.GREEN)
                    }

                    likeCount >= LEVEL2 -> {
                        ForegroundColorSpan(Color.YELLOW)
                    }

                    articles.like == LEVEL3 -> {
                        ForegroundColorSpan(Color.RED)
                    }

                    else -> {
                        ForegroundColorSpan(Color.GRAY)
                    }
                }
                setSpan(color, 0, articles.like.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            titleTv.text = articles.title
            authorTv.text = articles.author
            pinnedTv.visibility =
                if (articles.type == Type.PINNED_ARTICLES) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            dateTv.text = articles.date
            itemView.setOnClickListener {
                listener.invoke(itemView, articles)
            }
        }
    }

    companion object {
        private val level1 = 1..9
        private const val LEVEL2 = 10
        private const val LEVEL3 = "爆"

        private val diffCallback = object : DiffUtil.ItemCallback<Articles>() {
            override fun areItemsTheSame(oldItem: Articles, newItem: Articles): Boolean {
                return oldItem.url == oldItem.url
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Articles, newItem: Articles): Boolean {
                return oldItem == newItem
            }
        }
    }
}