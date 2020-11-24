package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

sealed class Action {
    data class LoadPosts(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isUpdatingFavoritesVisibility: Boolean = false
    ) : Action()

    data class PostsUpdated(
        val posts: List<Post>,
        val isRefreshing: Boolean = false,
        val isUpdatingFavoritesVisibility: Boolean = false
    ) : Action()

    data class ErrorUpdatingPosts(
        val error: Throwable,
        val isRefreshing: Boolean = false,
        val posts: List<Post>? = null
    ) : Action()

    object PostsCleared : Action()
}