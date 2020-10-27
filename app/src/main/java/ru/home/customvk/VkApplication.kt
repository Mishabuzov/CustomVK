package ru.home.customvk

import android.app.Application
import com.facebook.stetho.Stetho

class VkApplication : Application() {
    companion object {
        internal lateinit var instance: VkApplication
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}