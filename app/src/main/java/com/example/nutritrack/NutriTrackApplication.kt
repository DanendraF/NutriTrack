package com.example.nutritrack

import android.app.Application
import com.example.nutritrack.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NutriTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin {
            androidLogger(Level.ERROR) // Only show errors in production
            androidContext(this@NutriTrackApplication)
            modules(appModule) // Load all dependency modules
        }
    }
}
