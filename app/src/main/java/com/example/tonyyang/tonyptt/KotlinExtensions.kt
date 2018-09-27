package com.example.tonyyang.tonyptt

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData

/**
 * @author tonyyang
 * @date 2018/09/16
 */

fun <T> LiveData<T>.nonNullObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, android.arch.lifecycle.Observer {
        it?.let(observer)
    })
}