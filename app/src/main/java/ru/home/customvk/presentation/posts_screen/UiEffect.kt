package ru.home.customvk.presentation.posts_screen

sealed class UiEffect {
    object ScrollRecyclerToSavedPosition : UiEffect()

    class UpdateFavoritesVisibility(val areLikedPostsPresent: Boolean) : UiEffect()

    object FinishRefreshing : UiEffect()
}
