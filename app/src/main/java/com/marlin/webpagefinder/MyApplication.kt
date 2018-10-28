package com.marlin.webpagefinder

import android.app.Application
import com.marlin.webpagefinder.di.apiModule
import com.marlin.webpagefinder.di.presenterModule
import com.marlin.webpagefinder.di.repositoryModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin(this, listOf(repositoryModule, presenterModule, apiModule))

        // This will initialise Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}