package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

data class State(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingPosts: Boolean = false,
) {
    fun reduce(action: Action): State = when (action) {
        is Action.LoadPosts -> copy(
            isLoading = action.isLoading,
            isUpdatingPosts = false,
        )
        is Action.PostsUpdated -> copy(
            posts = action.posts,
            isLoading = false,
            isUpdatingPosts = true,
        )
        is Action.ErrorUpdatingPosts -> copy(
            posts = action.posts ?: posts,
            isLoading = false,
            isUpdatingPosts = true,
        )
    }
}
