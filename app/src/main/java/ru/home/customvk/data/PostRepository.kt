package ru.home.customvk.data

import io.reactivex.Single
import ru.home.customvk.VkApplication
import ru.home.customvk.data.api.ApiFactory.postApi
import ru.home.customvk.data.database.PostDao
import ru.home.customvk.domain.Post
import ru.home.customvk.utils.PostUtils.filterByFavorites
import ru.home.customvk.utils.PostUtils.toPosts

object RepositoryProvider {
    val postRepository: PostRepository = DefaultPostRepository()
}

private class DefaultPostRepository : PostRepository {

    private val postDao: PostDao = VkApplication.instance.database.postDao()

    private fun Single<List<Post>>.filterFavoritePostsIfNeeded(isFilterByFavorites: Boolean) =
        map { posts ->
            if (isFilterByFavorites) {
                posts.filterByFavorites()
            } else {
                posts
            }
        }

    override fun fetchPosts(forceUpdate: Boolean, isFilterByFavorites: Boolean): Single<List<Post>> {
        return if (forceUpdate) {
            fetchPostsFromInternet(isFilterByFavorites)
        } else {
            fetchPostsFromDatabase(isFilterByFavorites)
        }
    }

    private fun fetchPostsFromDatabase(isFilterByFavorites: Boolean): Single<List<Post>> {
        return if (isFilterByFavorites) {
            postDao.getFavoritePosts()
        } else {
            postDao.getPosts()
        }
    }

    private fun fetchPostsFromInternet(isFilterByFavorites: Boolean): Single<List<Post>> {
        return postApi.fetchNewsfeedWithPosts().map { response ->
            val posts = response.newsfeed.toPosts()
            if (posts.isNotEmpty()) {
                postDao.deleteAllPosts()
                postDao.savePosts(posts)
            }
            posts
        }.filterFavoritePostsIfNeeded(isFilterByFavorites)
    }


    override fun sendLikeRequest(post: Post, isPositiveLikeRequest: Boolean): Single<Post> =
        if (isPositiveLikeRequest) {
            postApi.likePost(post.postId, post.source.sourceId)
        } else {
            postApi.dislikePost(post.postId, post.source.sourceId)
        }.map {
            val updatedLikesCount = it.likesInfo.count
            val updatedPost = post.copy(likesCount = updatedLikesCount)
            postDao.updatePost(updatedPost)
            updatedPost
        }

    override fun hidePost(postToHide: Post): Single<Int> = postApi.ignorePost(postToHide.postId, postToHide.source.sourceId).map {
        postDao.deletePost(postToHide)
        it.vkResponseCode
    }
}

interface PostRepository {
    fun fetchPosts(forceUpdate: Boolean = false, isFilterByFavorites: Boolean = false): Single<List<Post>>

    fun sendLikeRequest(post: Post, isPositiveLikeRequest: Boolean): Single<Post>

    fun hidePost(postToHide: Post): Single<Int>
}