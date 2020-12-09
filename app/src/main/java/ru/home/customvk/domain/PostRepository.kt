package ru.home.customvk.domain

import io.reactivex.Single

interface PostRepository {
    fun fetchPosts(forceUpdate: Boolean = false, isFilterByFavorites: Boolean = false): Single<List<Post>>

    fun sendLikeRequest(post: Post, isPositiveLikeRequest: Boolean): Single<Post>

    fun hidePost(postToHide: Post): Single<Int>
}