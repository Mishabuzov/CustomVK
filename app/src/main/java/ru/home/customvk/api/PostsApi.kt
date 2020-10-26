package ru.home.customvk.api

import io.reactivex.Completable
import io.reactivex.Single
import ru.home.customvk.Post

interface PostsApi {

    fun fetchPosts(withUpdates: Boolean = false): Single<List<Post>>

    fun likePost(post: Post): Completable

}