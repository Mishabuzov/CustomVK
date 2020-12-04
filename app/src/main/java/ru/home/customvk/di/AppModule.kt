package ru.home.customvk.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.home.customvk.data.database.VkDatabase
import ru.home.customvk.utils.PreferencesUtils
import javax.inject.Singleton

@Module
class AppModule {

    private companion object {
        private const val DB_NAME = "vk_database"
    }

    @Provides
    @Singleton
    fun providePreferencesUtils(app: Application) = PreferencesUtils(app.applicationContext)

    @Provides
    @Singleton
    fun provideDatabase(app: Application) = Room.databaseBuilder(app, VkDatabase::class.java, DB_NAME).build()
}
