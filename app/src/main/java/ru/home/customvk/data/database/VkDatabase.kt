package ru.home.customvk.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.home.customvk.domain.Post

@Database(entities = [Post::class], version = 1)
abstract class VkDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}