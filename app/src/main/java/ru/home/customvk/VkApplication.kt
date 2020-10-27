package ru.home.customvk

import android.app.Application
import com.facebook.stetho.Stetho

class VkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}