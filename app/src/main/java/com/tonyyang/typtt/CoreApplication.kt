package com.tonyyang.typtt

import android.app.Application
import timber.log.Timber

class CoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}