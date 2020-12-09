package ru.home.customvk.presentation.posts_screen

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.home.customvk.VkApplication
import ru.home.customvk.domain.Post
import ru.home.customvk.domain.PostRepository
import ru.home.customvk.presentation.BaseRxViewModel
import ru.home.customvk.utils.PostUtils.areLikedPostsPresent
import ru.home.customvk.utils.PostUtils.likePostAtPosition
import ru.home.customvk.utils.PreferencesUtils
import ru.home.customvk.utils.SingleLiveEvent
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

    fun saveRecyclerPosition(positionToSave: Int) {
        preferencesUtils.saveRecyclerPosition(positionToSave, isAttachedToFavoritesFragment)
    }

    private fun logMessage(message: String) = Log.d(TAG, message)

    private fun logError(logErrorMessage: String, throwable: Throwable) = Log.e(TAG, logErrorMessage, throwable)
}
