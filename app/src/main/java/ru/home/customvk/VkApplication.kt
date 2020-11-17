package ru.home.customvk

import android.app.Application
import androidx.room.Room
import com.facebook.stetho.Stetho
import ru.home.customvk.data.database.VkDatabase

class VkApplication : Application() {

    companion object {
        internal lateinit var instance: VkApplication
    }

    lateinit var database: VkDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        Stetho.initializeWithDefaults(this)
        database = Room.databaseBuilder(this, VkDatabase::class.java, "vk_database").build()
    }
}