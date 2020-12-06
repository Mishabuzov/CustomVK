package ru.home.customvk.presentation.posts_screen

sealed class UiEffect {
    class UpdateFavoritesVisibility(val areLikedPostsPresent: Boolean) : UiEffect()

    object FinishRefreshing : UiEffect()

    object ErrorUpdatingPosts : UiEffect()

    class ScrollRecyclerToPosition(val position: Int) : UiEffect()
}
