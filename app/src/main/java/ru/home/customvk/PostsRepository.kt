package ru.home.customvk

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.home.customvk.api.PostsApi
import ru.home.customvk.api.PostsApiImpl

object PostsProvider {
    val postsRepository: PostsRepository = DefaultPostsRepository()
}

private class DefaultPostsRepository : PostsRepository {

    private val postsApi: PostsApi by lazy { PostsApiImpl() }

    private val hiddenPostIds: MutableSet<Int> = hashSetOf()

    private var wasUpdated = false

    private fun Single<List<Post>>.filterHiddenPosts() = map { posts -> posts.filter { it.id !in hiddenPostIds } }

    private fun Single<List<Post>>.filterFavoritePostsIfNeeded(isFilterByFavorites: Boolean) =
        map { posts ->
            if (isFilterByFavorites) {
                posts.filter(Post::isFavorite)
            } else {
                posts
            }
        }

    override fun fetchPosts(isFilterByFavorites: Boolean): Single<List<Post>> =
        postsApi.fetchPosts(wasUpdated)
            .filterHiddenPosts()
            .subscribeOn(Schedulers.io())
            .filterFavoritePostsIfNeeded(isFilterByFavorites)

    override fun notifyAboutUpdatingAction() {
        wasUpdated = true
    }

    override fun likePost(post: Post): Completable =
        postsApi.likePost(post)
            .subscribeOn(Schedulers.io())

    override fun saveHiddenPostId(postId: Int) = hiddenPostIds.add(postId)
}

interface PostsRepository {
    fun fetchPosts(isFilterByFavorites: Boolean): Single<List<Post>>
    fun likePost(post: Post): Completable
    fun saveHiddenPostId(postId: Int): Boolean
    fun notifyAboutUpdatingAction()
}