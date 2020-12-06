package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

sealed class Action {
    data class LoadPosts(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isFirstLoading: Boolean = false,
    ) : Action()

    data class PostsUpdated(
        val posts: List<Post>,
        val isFavoritesFragment: Boolean,
        val isFirstLoading: Boolean = false
    ) : Action()

    data class PostsUpdatedWithScrolling(
        val posts: List<Post>,
        val isFavoritesFragment: Boolean,
        val isScrollingToFirstPosition: Boolean,
        val isScrollingToSavedPosition: Boolean
    ) : Action()

    object PostsIsScrolled : Action()

    data class ErrorUpdatingPosts(val posts: List<Post>? = null) : Action()
}
