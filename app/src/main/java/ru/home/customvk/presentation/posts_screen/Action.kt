package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

sealed class Action {
    object LoadFirstPage : Action()

    data class PostsLoaded(val posts: List<Post>) : Action()

    object RefreshPosts : Action()

    data class FinishRefreshing(val posts: List<Post>) : Action()

    object SynchronizePosts : Action()

    data class FinishSynchronization(val posts: List<Post>) : Action()

    data class ErrorLoadingPosts(val error: Throwable) : Action()

    data class LikePost(val postIndex: Int, val isPositiveLike: Boolean = true) : Action()

    data class PostIsLiked(val posts: List<Post>) : Action()

    data class UpdatePostsLocally(val posts: List<Post>) : Action()

    data class ErrorLikePosts(val error: Throwable, val posts: List<Post>) : Action()

    data class HidePost(val postIndex: Int) : Action()

    data class PostIsHidden(val posts: List<Post>) : Action()

    data class ErrorHidePosts(val error: Throwable, val posts: List<Post>) : Action()
}