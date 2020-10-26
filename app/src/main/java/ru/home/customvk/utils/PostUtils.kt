package ru.home.customvk.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.home.customvk.Post

object PostUtils {

    private fun JSONObject.toPost() = Post(
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

    /**
     * Extracts Post objects from jsonString
     */
    fun parsePosts(jsonString: String): MutableList<Post> {
        val jsonArray = JSONArray(jsonString)
        val posts: MutableList<Post> = mutableListOf()
        for (i in 0 until jsonArray.length()) {
            posts.add(jsonArray.getJSONObject(i).toPost())
        }
        Log.d("M_PostUtils", "Altogether ${posts.size} posts are extracted.")
        return posts
    }
}