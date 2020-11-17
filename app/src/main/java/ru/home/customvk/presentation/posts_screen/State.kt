package ru.home.customvk.presentation.posts_screen

import ru.home.customvk.domain.Post

data class State(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingFinished: Boolean = false,
    val isSynchronizationNeeded: Boolean = false,
    val error: Throwable? = null,
    val likeStateInfo: LikeStateInfo? = null,
    val indexOfPostToHide: Int? = null
)

class LikeStateInfo(val positionOfPostToLike: Int, val isPositiveLike: Boolean)

internal fun State.reduce(action: Action): State = when (action) {
    is Action.PostsLoaded -> copy(
        posts = action.posts,
        isLoading = false,
    )
    is Action.ErrorLoadingPosts -> copy(
        isLoading = false,
        isUpdatingFinished = false,
        error = action.error
    )
    is Action.LoadFirstPage -> copy(
        isLoading = true,
        error = null,
        isUpdatingFinished = false,
    )
    is Action.SynchronizePosts -> copy(
        error = null,
        isUpdatingFinished = false,
    )
    is Action.FinishSynchronization -> copy(
        posts = action.posts,
        isSynchronizationNeeded = false
    )
    is Action.RefreshPosts -> copy(
        error = null,
    )
    is Action.FinishRefreshing -> copy(
        posts = action.posts,
        isSynchronizationNeeded = true,
        isUpdatingFinished = true,
    )
    is Action.LikePost -> copy(
        isUpdatingFinished = false,
        likeStateInfo = LikeStateInfo(action.postIndex, action.isPositiveLike),
        error = null
    )
    is Action.PostIsLiked -> copy(
        posts = action.posts,
        isSynchronizationNeeded = true,
        likeStateInfo = null
    )
    is Action.ErrorLikePosts -> copy(
        error = action.error,
        posts = action.posts,
        likeStateInfo = null
    )
    is Action.UpdatePostsLocally -> copy(
        error = null,
        posts = action.posts
    )
    is Action.HidePost -> copy(
        isUpdatingFinished = false,
        error = null,
        indexOfPostToHide = action.postIndex
    )
    is Action.PostIsHidden -> copy(
        indexOfPostToHide = null,
        posts = action.posts,
        isSynchronizationNeeded = true
    )
    is Action.ErrorHidePosts -> copy(
        indexOfPostToHide = null,
        posts = action.posts,
        error = action.error
    )
}
