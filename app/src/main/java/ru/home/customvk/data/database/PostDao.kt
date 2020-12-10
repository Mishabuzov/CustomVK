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

    @Query("Select * FROM ${Post.POSTS_TABLE_NAME}")
    fun getPosts(): Single<List<Post>>

    @Query("Select * FROM ${Post.POSTS_TABLE_NAME} where ${Post.COLUMN_IS_LIKED} = 1")
    fun getFavoritePosts(): Single<List<Post>>

    @Query("DELETE FROM ${Post.POSTS_TABLE_NAME}")
    fun deleteAllPosts()

    @Transaction
    fun replaceAllPosts(newPosts: List<Post>): List<Post> {
        deleteAllPosts()
        savePosts(newPosts)
        return newPosts
    }

    @Delete
    fun deletePost(post: Post)

}
