package ru.home.customvk.data

import io.reactivex.Single
import ru.home.customvk.data.api.PostApi
import ru.home.customvk.data.database.PostDao
import ru.home.customvk.domain.Post
import ru.home.customvk.domain.PostRepository
import ru.home.customvk.utils.PostUtils.filterByFavorites
import ru.home.customvk.utils.PostUtils.toPosts

class DefaultPostRepository(private val postDao: PostDao, private val postApi: PostApi) : PostRepository {

    private fun Single<List<Post>>.filterFavoritePostsIfNeeded(isFilterByFavorites: Boolean): Single<List<Post>> {
        return map { posts ->
            if (isFilterByFavorites) {
                posts.filterByFavorites()
            } else {
                posts
            }
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
            postDao.replaceAllPosts(response.newsfeed.toPosts())
        }.filterFavoritePostsIfNeeded(isFilterByFavorites)
    }

    override fun sendLikeRequest(post: Post, isPositiveLikeRequest: Boolean): Single<Post> {
        return if (isPositiveLikeRequest) {
            postApi.likePost(post.postId, post.source.sourceId)
        } else {
            postApi.dislikePost(post.postId, post.source.sourceId)
        }.map {
            val updatedLikesCount = it.likesInfo.count
            val updatedPost = post.copy(likesCount = updatedLikesCount)
            postDao.updatePost(updatedPost)
            updatedPost
        }
    }

    override fun hidePost(postToHide: Post): Single<Int> {
        return postApi.ignorePost(postToHide.postId, postToHide.source.sourceId)
            .map {
                postDao.deletePost(postToHide)
                it.vkResponseCode
            }
    }
}
