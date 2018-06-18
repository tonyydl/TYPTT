package com.example.tonyyang.tonyptt

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class DisposableManager private constructor(){

    companion object {
        val instance: DisposableManager by lazy { DisposableManager() }
    }

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun add(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun dispose() {
        compositeDisposable.dispose()
    }
}
