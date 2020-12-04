package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

sealed class Action {
    data class LoadPosts(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isNeedToScrollRecyclerToSavedPosition: Boolean = false,
    ) : Action()

    data class PostsUpdated(val posts: List<Post>) : Action()

    data class ErrorUpdatingPosts(val posts: List<Post>? = null) : Action()
}
