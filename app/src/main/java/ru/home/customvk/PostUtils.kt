package ru.home.customvk

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object PostUtils {
    private const val POSTS_STUB_JSON_FILE = "posts.json"
    private const val UPDATED_POSTS_STUB_JSON_FILE = "updated_posts.json"

    /**
     * Reads JSON-file from assets and returns it as a single String.
     */
    private fun readJson(fileName: String): String =
        VkApplication.instance.assets.open(fileName).bufferedReader().use { it.readText() }

    val initializationPosts: List<Post> by lazy { parsePosts(readJson(POSTS_STUB_JSON_FILE)) }

    val updatedPosts: List<Post> by lazy { parsePosts(readJson(UPDATED_POSTS_STUB_JSON_FILE)) }

    private fun JSONObject.toPost(): Post =
        Post(
            id = getInt("id"),
            groupName = getString("groupName"),
            groupLogo = getString("groupLogo"),
            date = getString("date"),
            textContent = getString("textContent"),
            pictureName = getString("pictureName"),
            isFavorite = getBoolean("isFavorite"),
            likesCount = getInt("likesCount"),
            commentsCount = getInt("commentsCount"),
            sharesCount = getInt("sharesCount"),
            viewings = getString("viewings")
        )

    fun updateLikesInfo(post: Post) =
        updatedPosts.find { it.id == post.id }?.let {
            it.likesCount = post.likesCount
            it.isFavorite = post.isFavorite
        }

    fun isLikedPostsPresent(): Boolean = initializationPosts.filter(Post::isFavorite).count() > 0

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
}