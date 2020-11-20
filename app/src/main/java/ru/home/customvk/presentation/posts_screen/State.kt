package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

data class State(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isNeedToSync: Boolean = false,
    val isUpdatingFavoritesVisibility: Boolean = false,
    val error: Throwable? = null,
)

private fun State.checkIfSyncIsNeeded(isNeedToSyncAfterUpdate: Boolean, isSyncCompleted: Boolean): Boolean {
    return when {
        isSyncCompleted -> false
        isNeedToSyncAfterUpdate -> true
        else -> isNeedToSync
    }
}

fun State.reduce(action: Action): State = when (action) {
    is Action.LoadPosts -> copy(
        isLoading = action.isLoading,
        isRefreshing = false,
        isUpdatingFavoritesVisibility = false,
        error = null,
    )
    is Action.PostsUpdated -> copy(
        posts = action.posts,
        isLoading = false,
        isRefreshing = action.isRefreshing,
        isUpdatingFavoritesVisibility = action.isUpdatingFavoritesVisibility,
        error = null,
        isNeedToSync = checkIfSyncIsNeeded(
            isNeedToSyncAfterUpdate = action.isNeedToSyncAfterUpdate,
            isSyncCompleted = action.isSyncCompleted
        ),
    )
    is Action.ErrorUpdatingPosts -> copy(
        posts = action.posts ?: posts,
        isLoading = false,
        isRefreshing = false,
        isUpdatingFavoritesVisibility = true,
        error = action.error,
    )
    Action.PostsCleared -> State(isNeedToSync = isNeedToSync)
}
