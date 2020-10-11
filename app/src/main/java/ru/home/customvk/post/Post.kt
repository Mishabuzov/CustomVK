package ru.home.customvk.post

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.home.customvk.VkApplication

class Post(
    val id: Int,
    val groupName: String,
    val groupLogo: String,
    val date: String,
    var textContent: String,
    val pictureName: String,
    var isFavorite: Boolean,
    var likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val viewings: String,
)

object PostUtils {
    private const val POSTS_STUB_JSON_FILE = "posts.json"
    private const val UPDATED_POSTS_STUB_JSON_FILE = "updated_posts.json"

    /**
     * Reads JSON-file from assets and returns it as a single String.
     */
    private fun readJson(fileName: String): String =
        VkApplication.instance.assets.open(fileName).bufferedReader().use { it.readText() }

    val initializationPosts: MutableList<Post> by lazy { parsePosts(readJson(POSTS_STUB_JSON_FILE)) }

    val updatedPosts: MutableList<Post> by lazy { parsePosts(readJson(UPDATED_POSTS_STUB_JSON_FILE)) }

    private fun JSONObject.toPost(): Post =
        Post(
            getInt("id"),
            getString("groupName"),
            getString("groupLogo"),
            getString("date"),
            getString("textContent"),
            getString("pictureName"),
            getBoolean("isFavorite"),
            getInt("likesCount"),
            getInt("commentsCount"),
            getInt("sharesCount"),
            getString("viewings")
        )

    fun updateLikesInfo(post: Post) =
        updatedPosts.find { it.id == post.id }?.let {
            it.likesCount = post.likesCount
            it.isFavorite = post.isFavorite
        }

    fun removePostFromUpdates(postId: Int) =
        updatedPosts.remove(updatedPosts.find { it.id == postId })

    /**
     * Extracts Post objects from jsonString
     */
    private fun parsePosts(jsonString: String): MutableList<Post> {
        val jsonArray = JSONArray(jsonString)
        val posts: MutableList<Post> = mutableListOf()
        for (i in 0 until jsonArray.length()) {
            posts.add(jsonArray.getJSONObject(i).toPost())
        }
        Log.d("M_PostUtils", "Altogether ${posts.size} posts are extracted.")
        return posts
    }

    /**
     * Compares visible content of two posts.
     */
    fun isVisibleContentEquals(firstPost: Post, secondPost: Post) =
        firstPost.groupName == secondPost.groupName
                && firstPost.groupLogo == secondPost.groupLogo
                && firstPost.date == secondPost.date
                && firstPost.textContent == secondPost.textContent
                && firstPost.pictureName == secondPost.pictureName
                && firstPost.isFavorite == secondPost.isFavorite
                && firstPost.likesCount == secondPost.likesCount
                && firstPost.commentsCount == secondPost.commentsCount
                && firstPost.sharesCount == secondPost.sharesCount
                && firstPost.viewings == secondPost.viewings
}