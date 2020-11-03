package ru.home.customvk

import io.reactivex.Single
import ru.home.customvk.api.ApiFactory.postApi
import ru.home.customvk.database.PostDao
import ru.home.customvk.models.local.Post
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

    private fun fetchPostsFromInternet() = postApi.fetchNewsfeedWithPosts().map { response -> response.newsfeed.toPosts() }

    override fun loadPostsFromDatabase(isFilterByFavorites: Boolean): Single<List<Post>> =
        if (isFilterByFavorites) {
            postDao.getFavoritePosts()
        } else {
            postDao.getPosts()
        }

    private fun downloadAndSavePosts(): Single<List<Post>> = fetchPostsFromInternet().map { downloadedPosts ->
        postDao.deleteAllPosts()
        postDao.savePosts(downloadedPosts)
        downloadedPosts
    }

    override fun fetchPostsFromInternet(isFilterByFavorites: Boolean): Single<List<Post>> = downloadAndSavePosts()
        .filterFavoritePostsIfNeeded(isFilterByFavorites)

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
    fun fetchPostsFromInternet(isFilterByFavorites: Boolean): Single<List<Post>>
    fun loadPostsFromDatabase(isFilterByFavorites: Boolean): Single<List<Post>>
    fun sendLikeRequest(post: Post, isPositiveLikeRequest: Boolean): Single<Post>
    fun hidePost(postToHide: Post): Single<Int>
}