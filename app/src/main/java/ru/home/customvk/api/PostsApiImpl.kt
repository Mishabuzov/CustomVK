package ru.home.customvk.api

import io.reactivex.Completable
import io.reactivex.Single
import ru.home.customvk.Post
import ru.home.customvk.VkApplication
import ru.home.customvk.utils.PostUtils

class PostsApiImpl : PostsApi {

    private companion object {
        private const val POSTS_STUB_JSON_FILE = "posts.json"
        private const val UPDATED_POSTS_STUB_JSON_FILE = "updated_posts.json"
    }

    private val initializationPosts: List<Post> by lazy { parseJsonWithPosts() }
    private val updatedPosts: List<Post> by lazy { parseJsonWithPosts(withUpdates = true) }

    override fun fetchPosts(withUpdates: Boolean): Single<List<Post>> = Single.fromCallable {
        if (withUpdates) {
            updatedPosts
        } else {
            initializationPosts
        }
    }

    override fun likePost(post: Post): Completable = Completable.fromCallable {
        updateLikesInPostsList(post, initializationPosts)
        updateLikesInPostsList(post, updatedPosts)
    }

    private fun updateLikesInPostsList(post: Post, posts: List<Post>) = posts.find { it.id == post.id }?.let {
        it.likesCount = post.likesCount
        it.isFavorite = post.isFavorite
    }

    private fun parseJsonWithPosts(withUpdates: Boolean = false): List<Post> = PostUtils.parsePosts(readJson(getJsonFile(withUpdates)))

    private fun getJsonFile(withUpdates: Boolean) =
        if (withUpdates) {
            UPDATED_POSTS_STUB_JSON_FILE
        } else {
            POSTS_STUB_JSON_FILE
        }

    private fun readJson(fileName: String): String = VkApplication.instance.assets.open(fileName).bufferedReader().use { it.readText() }
}