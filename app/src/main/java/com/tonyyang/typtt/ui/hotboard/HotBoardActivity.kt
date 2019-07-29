package com.tonyyang.typtt.ui.hotboard

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tonyyang.typtt.LoadingEffectSupport
import com.tonyyang.typtt.R
import com.tonyyang.typtt.ToolbarSupport
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar
import kotlinx.android.synthetic.main.hotboard_activity.*

class HotBoardActivity : AppCompatActivity(), ToolbarSupport, LoadingEffectSupport {
    override fun startLoadingBar() {
        if (progressbar is SmoothProgressBar) {
            (progressbar as SmoothProgressBar).progressiveStart()
            progressbar.visibility = View.VISIBLE
        }
    }

    override fun stopLoadingBar() {
        if (progressbar is SmoothProgressBar) {
            (progressbar as SmoothProgressBar).progressiveStart()
            progressbar.visibility = View.GONE
        }
    }

    override fun updateTitle(title: String, subTitle: String) {
        supportActionBar?.title = title
        supportActionBar?.subtitle = subTitle
    }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotboard_activity)
        setSupportActionBar(toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, HotBoardFragment.newInstance())
                    .commitNow()
        }
    }

}
