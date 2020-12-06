package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

data class State(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingPosts: Boolean = false,
    val isEmptyState: Boolean = false
) {
    fun reduce(action: Action): State = when (action) {
        is Action.LoadPosts -> copy(
            isLoading = action.isLoading,
            isUpdatingPosts = false,
            isEmptyState = false
        )
        is Action.PostsUpdated -> copy(
            posts = action.posts,
            isLoading = false,
            isUpdatingPosts = true,
            // don't show empty state on very 1st loading, because immediately after that refresh query is started.
            isEmptyState = !action.isFavoritesFragment && action.posts.isEmpty() && !action.isFirstLoading
        )
        is Action.ErrorUpdatingPosts -> {
            val postsToShow = action.posts ?: posts
            copy(
                posts = postsToShow,
                isLoading = false,
                isUpdatingPosts = true,
                isEmptyState = postsToShow.isEmpty()
            )
        }
        is Action.PostsUpdatedWithScrolling -> copy(
            posts = action.posts,
            isLoading = false,
            isUpdatingPosts = true,
            isEmptyState = !action.isFavoritesFragment && action.posts.isEmpty()
        )
        Action.PostsIsScrolled -> copy(isUpdatingPosts = false)
    }
}
