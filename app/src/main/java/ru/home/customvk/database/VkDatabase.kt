package ru.home.customvk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.home.customvk.models.local.Post

@Database(entities = [Post::class], version = 1)
abstract class VkDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}