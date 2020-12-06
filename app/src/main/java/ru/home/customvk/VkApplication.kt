package ru.home.customvk

import android.app.Application
import com.facebook.stetho.Stetho
import ru.home.customvk.di.AppComponent
import ru.home.customvk.di.DaggerAppComponent

class VkApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)

        appComponent = DaggerAppComponent.builder().withApplication(this).build()
    }
}
