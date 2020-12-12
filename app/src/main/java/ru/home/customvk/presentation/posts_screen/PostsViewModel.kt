package ru.home.customvk.presentation.posts_screen

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.home.customvk.VkApplication
import ru.home.customvk.domain.Post
import ru.home.customvk.domain.PostRepository
import ru.home.customvk.presentation.BaseRxViewModel
import ru.home.customvk.utils.AttachmentUtils.PUBLIC_IMAGES_DIR
import ru.home.customvk.utils.AttachmentUtils.compressBitmap
import ru.home.customvk.utils.PostUtils
import ru.home.customvk.utils.PostUtils.areLikedPostsPresent
import ru.home.customvk.utils.PostUtils.likePostAtPosition
import ru.home.customvk.utils.PreferencesUtils
import ru.home.customvk.utils.SingleLiveEvent
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.properties.Delegates

private typealias PostSideEffect = SideEffect<State, out Action>

class PostsViewModel(application: Application) : BaseRxViewModel(application) {

    @Inject
    lateinit var postRepository: PostRepository

    @Inject
    lateinit var preferencesUtils: PreferencesUtils

    private companion object {
        private const val TAG = "POSTS_VIEW_MODEL"

        // small delay to prevent flickering visual effects and recycler's inconsistency states.
        private const val UPDATING_DELAY_MILLIS = 300L
    }

    private val inputRelay: Relay<Action> = PublishRelay.create()
    private val input: Consumer<Action> get() = inputRelay
    private val state: Observable<State> = inputRelay.reduxStore(
        initialState = State(),
        sideEffects = listOf(loadPosts(), updatePostsWithScrolling()),
        reducer = { state, action -> state.reduce(action) }
    )
    private val currentState: MutableLiveData<State> = MutableLiveData()
    fun getStateLiveData() = currentState as LiveData<State>

    private val uiEffectsRelay = PublishRelay.create<UiEffect>()
    private val uiEffectsInput: Consumer<UiEffect> get() = uiEffectsRelay
    private val uiEffectsState: Observable<UiEffect> get() = uiEffectsRelay
    private val currentUiEffects: SingleLiveEvent<UiEffect> = SingleLiveEvent()
    fun getUiEffectsLiveData() = currentUiEffects as LiveData<UiEffect>

    private var posts: List<Post> = emptyList()
    private var isAttachedToFavoritesFragment by Delegates.notNull<Boolean>()

    init {
        (application as VkApplication).appComponent.postsViewModelSubComponentBuilder().build().inject(this)
    }

    fun onAttachViewModel(isFilterByFavorites: Boolean, isFirstLoading: Boolean) {
        this.isAttachedToFavoritesFragment = isFilterByFavorites

        state.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentState.value = it }
            .disposeOnFinish()
        uiEffectsState.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentUiEffects.value = it }
            .disposeOnFinish()

        input.accept(Action.LoadPosts(isFirstLoading = isFirstLoading))  // show cached posts firstly.
        if (!isFilterByFavorites && isFirstLoading) {
            // silent update from network on 1st loading. Delay is added to prevent inconsistency of update in the recycler.
            Handler(Looper.getMainLooper()).postDelayed({ refreshPosts(true) }, UPDATING_DELAY_MILLIS)
        }
    }

    fun refreshPosts(showLoadingState: Boolean = false) = input.accept(Action.LoadPosts(isLoading = showLoadingState, isRefreshing = true))

    private fun loadPosts(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.LoadPosts::class.java)
                .flatMap { loadingAction ->
                    postRepository.fetchPosts(forceUpdate = loadingAction.isRefreshing, isFilterByFavorites = isAttachedToFavoritesFragment)
                        .subscribeOn(Schedulers.io())
                        .toObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            val werePostsBeforeUpdate = posts.isNotEmpty()  // if were not posts -> no need to scroll up on update
                            posts = it
                            activateOnFinishLoadingUiEffects(loadingAction.isRefreshing)
                            val isScrollingToFirstPosition = loadingAction.isRefreshing && posts.size > 1 && werePostsBeforeUpdate
                            val isScrollingToSavedPosition = !loadingAction.isRefreshing && !loadingAction.isFirstLoading
                            if (isScrollingToFirstPosition || isScrollingToSavedPosition) {
                                Action.PostsUpdatedWithScrolling(
                                    posts = posts,
                                    isScrollingToFirstPosition = isScrollingToFirstPosition,
                                    isScrollingToSavedPosition = isScrollingToSavedPosition,
                                    isFavoritesFragment = isAttachedToFavoritesFragment
                                )
                            } else {
                                Action.PostsUpdated(
                                    posts = posts,
                                    isFavoritesFragment = isAttachedToFavoritesFragment,
                                    isFirstLoading = loadingAction.isFirstLoading
                                )
                            }
                        }
                        .onErrorReturn { error ->
                            activateOnFinishLoadingUiEffects(loadingAction.isRefreshing)
                            logError("Error updating posts, force update = ${loadingAction.isRefreshing}", error)
                            uiEffectsInput.accept(UiEffect.ErrorUpdatingPosts)
                            Action.ErrorUpdatingPosts()
                        }
                }
        }
    }

    private fun updatePostsWithScrolling(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.PostsUpdatedWithScrolling::class.java)
                .flatMap { updatingAction ->
                    Observable.fromCallable {
                        when {
                            updatingAction.isScrollingToFirstPosition -> uiEffectsInput.accept(UiEffect.ScrollRecyclerToPosition(0))
                            updatingAction.isScrollingToSavedPosition -> {
                                uiEffectsInput.accept(
                                    UiEffect.ScrollRecyclerToPosition(preferencesUtils.getRecyclerPosition(isAttachedToFavoritesFragment))
                                )
                            }
                        }
                        Action.PostsUpdated(posts = posts, isFavoritesFragment = isAttachedToFavoritesFragment)
                    }
                }
        }
    }

    private fun activateOnFinishLoadingUiEffects(isRefreshing: Boolean) {
        updateFavoritesVisibility()
        if (isRefreshing) {
            uiEffectsInput.accept(UiEffect.FinishRefreshing)
        }
    }

    private fun updateFavoritesVisibility() = uiEffectsInput.accept(UiEffect.UpdateFavoritesVisibility(posts.areLikedPostsPresent()))

    /**
     * process user's clicking by like button
     */
    fun processLike(postIndex: Int) {
        val updatedPosts = posts.toMutableList()
        val likedPost = updatedPosts.likePostAtPosition(postIndex)
        if (isAttachedToFavoritesFragment) {
            updatedPosts.removeAt(postIndex)
        }
        input.accept(Action.PostsUpdated(posts = updatedPosts, isFavoritesFragment = isAttachedToFavoritesFragment))

        processLikeRequest(updatedPosts, likedPost, postIndex)
    }

    private fun processLikeRequest(updatedPosts: MutableList<Post>, likedPost: Post, postIndex: Int) {
        postRepository.sendLikeRequest(likedPost, likedPost.isLiked)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatedPost ->
                    logMessage("Success like request of post at position $postIndex")
                    posts = updatedPosts
                    if (updatedPost.likesCount != likedPost.likesCount && !isAttachedToFavoritesFragment) {
                        (posts as MutableList<Post>)[postIndex] = updatedPost
                    }
                    input.accept(Action.PostsUpdated(posts = posts, isFavoritesFragment = isAttachedToFavoritesFragment))
                    updateFavoritesVisibility()
                },
                { throwable ->
                    logError("Fail to like post at $postIndex position", throwable)
                    updateOnErrorWithDelay()
                }
            )
            .disposeOnFinish()
    }

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        val updatedPosts = posts.toMutableList()
        updatedPosts.removeAt(postIndex)
        input.accept(Action.PostsUpdated(posts = updatedPosts, isFavoritesFragment = isAttachedToFavoritesFragment))

        requestToHidePost(postIndex, updatedPosts)
    }

    private fun requestToHidePost(postIndex: Int, updatedPosts: List<Post>) {
        val postToHide: Post = posts[postIndex]
        postRepository.hidePost(postToHide)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { responseCode ->
                    posts = updatedPosts
                    logMessage("post is hidden, response code: $responseCode")
                    input.accept(Action.PostsUpdated(posts = posts, isFavoritesFragment = isAttachedToFavoritesFragment))
                    updateFavoritesVisibility()
                },
                { throwable ->
                    logError("fail to hide post at $postIndex position", throwable)
                    updateOnErrorWithDelay()
                }
            )
            .disposeOnFinish()
    }

    private fun updateOnErrorWithDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            input.accept(Action.ErrorUpdatingPosts(posts = posts))
            uiEffectsInput.accept(UiEffect.ErrorUpdatingPosts)
            updateFavoritesVisibility()
        }, UPDATING_DELAY_MILLIS)
    }

    private fun getAppContext() = getApplication<VkApplication>().applicationContext

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveBitmapThroughMediaStore(bitmap: Bitmap, bitmapFullName: String, imageMimeType: String) {
        Single.fromCallable {
            val resolver: ContentResolver = getAppContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, bitmapFullName)
                put(MediaStore.MediaColumns.MIME_TYPE, imageMimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_IMAGES_DIR)
            }
            val localImageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            resolver.openOutputStream(localImageUri)!!.compressBitmap(bitmap)
            Pair<Uri, String>(localImageUri, imageMimeType)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { imageInfo ->
                    uiEffectsInput.accept(
                        UiEffect.ShowDialogToOpenImageInOtherApp(imageUri = imageInfo.first, imageMimeType = imageInfo.second)
                    )
                    uiEffectsInput.accept(UiEffect.ShowSuccessSavingToGalleryNotification)
                },
                { throwable ->
                    logError("Error saving image to gallery through media store!", throwable)
                    uiEffectsInput.accept(UiEffect.ErrorSharingImage)
                }
            )
            .disposeOnFinish()
    }

    /**
     * This function uses deprecated way of saving image to gallery. It is used only if SDK version < Android Q
     */
    fun saveBitmapInExternalStorageDir(bitmap: Bitmap, bitmapFullName: String) {
        Single.fromCallable {
            @Suppress("DEPRECATION")
            val imageFileToSave = File(Environment.getExternalStoragePublicDirectory(PUBLIC_IMAGES_DIR), bitmapFullName)
            FileOutputStream(imageFileToSave).compressBitmap(bitmap)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { uiEffectsInput.accept(UiEffect.ShowSuccessSavingToGalleryNotification) },
                { throwable ->
                    logError("Error saving image to gallery through ExternalStoragePublicDirectory!", throwable)
                    uiEffectsInput.accept(UiEffect.ErrorSharingImage)
                }
            )
            .disposeOnFinish()
    }

    /**
     * Caches image-bitmap to internal dir and returns its Uri.
     */
    fun cacheBitmapForSharing(bitmap: Bitmap, bitmapFullName: String) {
        Single.fromCallable {
            val imageFile: File = PostUtils.createFileToCacheBitmap(bitmapFullName, getAppContext().cacheDir)
            FileOutputStream(imageFile).compressBitmap(bitmap)
            FileProvider.getUriForFile(getAppContext(), PostUtils.POSTS_IMAGE_PROVIDER_AUTHORITIES, imageFile)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { imageUri -> uiEffectsInput.accept(UiEffect.ShareImage(imageUri)) },
                { throwable ->
                    logError("Failed to cache image for sharing!", throwable)
                    uiEffectsInput.accept(UiEffect.ErrorSharingImage)
                }
            )
            .disposeOnFinish()
    }

    fun saveRecyclerPosition(positionToSave: Int) {
        preferencesUtils.saveRecyclerPosition(positionToSave, isAttachedToFavoritesFragment)
    }

    private fun logMessage(message: String) = Log.d(TAG, message)

    private fun logError(logErrorMessage: String, throwable: Throwable) = Log.e(TAG, logErrorMessage, throwable)
}
