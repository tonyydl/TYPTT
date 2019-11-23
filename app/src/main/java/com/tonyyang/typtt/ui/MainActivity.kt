package com.tonyyang.typtt.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tonyyang.typtt.R
import com.tonyyang.typtt.setupActionBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(false)
        }
    }
}
