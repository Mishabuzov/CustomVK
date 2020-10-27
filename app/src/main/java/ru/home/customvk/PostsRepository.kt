package ru.home.customvk

import io.reactivex.Single
import ru.home.customvk.api.ApiFactory.postsApi
import ru.home.customvk.models.local.Post
import ru.home.customvk.utils.PostUtils.filterByFavorites
import ru.home.customvk.utils.PostUtils.toPosts

object PostsProvider {
    val postsRepository: PostsRepository = DefaultPostsRepository()
}

private class DefaultPostsRepository : PostsRepository {

    private val hiddenPostIds: MutableSet<Pair<Long, Long>> = hashSetOf()

    private fun Single<List<Post>>.filterHiddenPosts() = map { posts -> posts.filter { it.id to it.source.id !in hiddenPostIds } }

    private fun Single<List<Post>>.filterFavoritePostsIfNeeded(isFilterByFavorites: Boolean) =
        map { posts ->
            if (isFilterByFavorites) {
                posts.filterByFavorites()
            } else {
                posts
            }
        }

    override fun fetchPosts(isFilterByFavorites: Boolean): Single<List<Post>> = postsApi.fetchNewsfeedWithPosts()
        .map { response -> response.newsfeed.toPosts() }
        .filterHiddenPosts()
        .filterFavoritePostsIfNeeded(isFilterByFavorites)

    override fun likePost(post: Post): Single<Int> = postsApi.likePost(post.id, post.source.id).map { it.likesInfo.count }
    override fun dislikePost(post: Post): Single<Int> = postsApi.dislikePost(post.id, post.source.id).map { it.likesInfo.count }

    override fun rememberHiddenPost(hiddenPost: Post) = hiddenPostIds.add(hiddenPost.id to hiddenPost.source.id)
}

interface PostsRepository {
    fun fetchPosts(isFilterByFavorites: Boolean): Single<List<Post>>
    fun likePost(post: Post): Single<Int>
    fun dislikePost(post: Post): Single<Int>
    fun rememberHiddenPost(hiddenPost: Post): Boolean
}