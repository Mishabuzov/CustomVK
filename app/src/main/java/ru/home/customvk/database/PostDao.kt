package ru.home.customvk.database

import androidx.room.*
import io.reactivex.Single
import ru.home.customvk.models.local.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePosts(newPosts: List<Post>)

    @Update
    fun updatePost(post: Post)

    @Query("Select * FROM post")
    fun getPosts(): Single<List<Post>>

    @Query("Select * FROM post where is_liked = 1")
    fun getFavoritePosts(): Single<List<Post>>

    // TODO: implement DELETE Query when updating by time.
//    @Query("DELETE FROM post")
//    fun deleteAllPosts()

}