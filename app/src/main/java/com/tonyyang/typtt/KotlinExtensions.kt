package com.tonyyang.typtt

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun AppCompatActivity.setupActionBar(action: ActionBar.() -> Unit) {
    supportActionBar?.run {
        action()
    }
}