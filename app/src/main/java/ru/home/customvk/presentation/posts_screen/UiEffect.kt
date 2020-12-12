package ru.home.customvk.presentation.posts_screen

import android.net.Uri

sealed class UiEffect {
    class UpdateFavoritesVisibility(val areLikedPostsPresent: Boolean) : UiEffect()

    object FinishRefreshing : UiEffect()

    object ErrorUpdatingPosts : UiEffect()

    class ScrollRecyclerToPosition(val position: Int) : UiEffect()

    class ShareImage(val internalImageUri: Uri) : UiEffect()

    object ErrorSharingImage : UiEffect()

    class ShowDialogToOpenImageInOtherApp(val imageUri: Uri, val imageMimeType: String) : UiEffect()

    object ShowSuccessSavingToGalleryNotification : UiEffect()
}
