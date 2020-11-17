package ru.home.customvk.data.database

import androidx.room.*
import io.reactivex.Single
import ru.home.customvk.domain.Post

@Dao
interface PostDao {

    @Insert
    fun savePosts(newPosts: List<Post>)

    @Update
    fun updatePost(post: Post)

    @Query("Select * FROM post")
    fun getPosts(): Single<List<Post>>

    @Query("Select * FROM post where is_liked = 1")
    fun getFavoritePosts(): Single<List<Post>>

    @Query("DELETE FROM post")
    fun deleteAllPosts()

    @Delete
    fun deletePost(post: Post)

}