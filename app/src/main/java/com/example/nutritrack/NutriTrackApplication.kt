package com.example.nutritrack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NutriTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
