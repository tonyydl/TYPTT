package com.tonyyang.typtt.ui.hotboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.tonyyang.typtt.R
import com.tonyyang.typtt.databinding.ViewPopularityBinding

class HotBoardPopularityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var binding: ViewPopularityBinding =
        ViewPopularityBinding.inflate(LayoutInflater.from(context), this, true)

    fun show(number: Int) {
        when {
            number >= MAX_NUMBER -> showIcon()
            number in MIN_NUMBER..MAX_NUMBER -> showNumber(number)
            else -> hidden()
        }
    }

    private fun showIcon() {
        binding.tvNumber.visibility = View.GONE
        binding.ivIcon.visibility = View.VISIBLE
        visibility = View.VISIBLE
    }

    private fun showNumber(number: Int) {
        binding.tvNumber.run {
            text = "".plus(number)
            visibility = View.VISIBLE
        }
        binding.ivIcon.visibility = View.GONE
        visibility = View.VISIBLE
    }

    private fun hidden() {
        visibility = View.GONE
    }

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.HotBoardPopularityView)
            if (a.hasValue(R.styleable.HotBoardPopularityView_popularity_number)) {
                val number = a.getInt(R.styleable.HotBoardPopularityView_popularity_number, 0)
                show(number)
            }
            a.recycle()
        }
    }

    companion object {
        const val MIN_NUMBER: Int = 1
        const val MAX_NUMBER: Int = 2000
    }
}