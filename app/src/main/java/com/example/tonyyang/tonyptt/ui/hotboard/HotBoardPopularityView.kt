package com.example.tonyyang.tonyptt.ui.hotboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.example.tonyyang.tonyptt.R
import kotlinx.android.synthetic.main.popularity_view.view.*

class HotBoardPopularityView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        const val MIN_NUMBER: Int = 1
        const val MAX_NUMBER: Int = 2000
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.popularity_view, this, true)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.HotBoardPopularityView)
            if (a.hasValue(R.styleable.HotBoardPopularityView_popularity_number)) {
                val number = a.getInt(R.styleable.HotBoardPopularityView_popularity_number, 0)
                show(number)
            }
            a.recycle()
        }
    }

    fun show(number: Int) {
        when {
            number >= MAX_NUMBER -> showIcon()
            number in MIN_NUMBER..MAX_NUMBER -> showNumber(number)
            else -> hidden()
        }
    }

    private fun showIcon() {
        number_tv.visibility = View.GONE
        icon_iv.visibility = View.VISIBLE
        visibility = View.VISIBLE
    }

    private fun showNumber(number: Int) {
        number_tv.text = "".plus(number)
        number_tv.visibility = View.VISIBLE
        icon_iv.visibility = View.GONE
        visibility = View.VISIBLE
    }

    private fun hidden() {
        visibility = View.GONE
    }

}