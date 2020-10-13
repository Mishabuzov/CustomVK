package ru.home.customvk.posts_screen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.home.customvk.Post
import ru.home.customvk.PostUtils

class PostsFragmentViewModel : ViewModel() {
    val posts: MutableLiveData<List<Post>> = MutableLiveData()

    private var isUpdated = false

    var isFilterByFavorites = false

    private fun List<Post>.filterFavoritesIfNeeded(): List<Post> {
        if (isFilterByFavorites) {
            return filter(Post::isFavorite)
        }
        return this
    }

    fun initPosts() {
        posts.value = PostUtils.initializationPosts.filterFavoritesIfNeeded()
    }

    fun refreshPosts() {
        posts.value = PostUtils.updatedPosts.filterFavoritesIfNeeded()
        isUpdated = true
    }

    fun likePostInFeed(post: Post): Int {
        val matchedPost = posts.value?.find { it.id == post.id }!!.apply {
            isFavorite = post.isFavorite
            likesCount = post.likesCount
        }
        return posts.value!!.indexOf(matchedPost)
    }

    fun onLikePostAction(post: Post) {
        if (post.isFavorite) {
            post.isFavorite = false
            post.likesCount--
            if (isFilterByFavorites) {
                posts.value = posts.value?.filter { post != it }
            }
        } else {
            post.isFavorite = true
            post.likesCount++
        }
        if (!isUpdated) {  // in case there was not update - update likes in the same post in updated version.
            PostUtils.updateLikesInfo(post)
        }
    }

    fun onRemovingSwipeAction(postId: Int) {
        posts.value = posts.value?.filter { it.id != postId }
    }
}