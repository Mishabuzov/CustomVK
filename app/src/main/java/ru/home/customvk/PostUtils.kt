package ru.home.customvk

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object PostUtils {

  /*  private val hiddenPostIds: MutableSet<Int> = hashSetOf()

    private var isUpdated = false*/

    /*fun getPosts() = if (isUpdated) {
        updatedPosts
    } else {
        initializationPosts
    }.filterHiddenPosts()*/

    /*fun refreshPosts(): List<Post> {
        isUpdated = true
        return getPosts()
    }*/

//    fun hidePost(postId: Int) = hiddenPostIds.add(postId)

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

    /* fun updateLikesInfo(post: Post) {
         updateLikesInPostsList(post, initializationPosts)
         updateLikesInPostsList(post, updatedPosts)
     }

     private fun updateLikesInPostsList(post: Post, posts: List<Post>) =
         posts.find { it.id == post.id }?.let {
             it.likesCount = post.likesCount
             it.isFavorite = post.isFavorite
         }*/

//    fun areLikedPostsPresent(): Boolean = getPosts().filter(Post::isFavorite).count() > 0

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