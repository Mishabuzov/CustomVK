package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

data class State(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLocalUpdating: Boolean = false,
    val isUpdatingFavoritesVisibility: Boolean = false,
    val error: Throwable? = null,
)

fun State.reduce(action: Action): State = when (action) {
    is Action.LoadPosts -> copy(
        isLoading = action.isLoading,
        isRefreshing = false,
        isLocalUpdating = false,
        isUpdatingFavoritesVisibility = false,
        error = null,
    )
    is Action.PostsUpdated -> copy(
        posts = action.posts,
        isLoading = false,
        isRefreshing = action.isRefreshing,
        isLocalUpdating = true,
        isUpdatingFavoritesVisibility = action.isUpdatingFavoritesVisibility,
        error = null
    )
    is Action.ErrorUpdatingPosts -> copy(
        posts = action.posts ?: posts,
        isLoading = false,
        isRefreshing = action.isRefreshing,
        isLocalUpdating = true,
        isUpdatingFavoritesVisibility = true,
        error = action.error,
    )
    is Action.PostsCleared -> State(posts = posts)
}
