package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

data class State(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingPosts: Boolean = false,
    val error: Throwable? = null,
)

fun State.reduce(action: Action): State = when (action) {
    is Action.LoadPosts -> copy(
        isLoading = action.isLoading,
        isUpdatingPosts = false,
        error = null,
    )
    is Action.PostsUpdated -> copy(
        posts = action.posts,
        isLoading = false,
        isUpdatingPosts = true,
        error = null
    )
    is Action.ErrorUpdatingPosts -> copy(
        posts = action.posts ?: posts,
        isLoading = false,
        isUpdatingPosts = true,
        error = action.error,
    )
    is Action.ChangeFragment -> State(posts = posts)
}
