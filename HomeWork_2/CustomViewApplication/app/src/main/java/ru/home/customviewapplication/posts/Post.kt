package ru.home.customviewapplication.posts

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class Post(
    val id: Int,
    val groupName: String,
    val groupLogo: String,
    val date: String,
    var textContent: String,
    val pictureName: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val viewings: String
)

object PostUtils {
    internal const val POSTS_STUB_JSON_FILE = "posts.json"

    private fun JSONObject.toPost() =
        Post(
            getInt("id"),
            getString("groupName"),
            getString("groupLogo"),
            getString("date"),
            getString("textContent"),
            getString("pictureName"),
            getInt("likesCount"),
            getInt("commentsCount"),
            getInt("sharesCount"),
            getString("viewings")
        )

    internal fun parsePosts(jsonString: String): List<Post> {
        val jsonArray = JSONArray(jsonString)
        val posts = mutableListOf<Post>()
        for (i in 0 until jsonArray.length()) {
            posts.add(jsonArray.getJSONObject(i).toPost())
        }
        Log.d("M_PostUtils", "Altogether ${posts.size} posts are extracted.")
        return posts
    }
}