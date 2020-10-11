package ru.home.customvk

import android.app.Application

class VkApplication : Application() {
    companion object {
        internal lateinit var instance: VkApplication
    }

    init {
        instance = this
    }
}