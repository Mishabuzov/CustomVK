package ru.home.customvk.presentation.posts_screen

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.home.customvk.data.RepositoryProvider
import ru.home.customvk.domain.Post
import ru.home.customvk.presentation.BaseRxViewModel
import ru.home.customvk.utils.PostUtils.areLikedPostsPresent
import ru.home.customvk.utils.PostUtils.likePostAtPosition

private typealias PostSideEffect = SideEffect<State, out Action>

class PostsViewModel(private val isFilterByFavorites: Boolean, private val isFirstLoading: Boolean) : BaseRxViewModel() {

    private companion object {
        private const val TAG = "POSTS_VIEW_MODEL"

        // small delay to prevent flickering visual effects and recycler's inconsistency states.
        private const val UPDATING_DELAY_MILLIS = 300L
    }

    private val inputRelay: Relay<Action> = PublishRelay.create()
    private val input: Consumer<Action> get() = inputRelay
    private var state: Observable<State> = inputRelay.reduxStore(
        initialState = State(),
        sideEffects = listOf(loadPosts()),
        reducer = { state, action -> state.reduce(action) }
    )

    private val currentState: MutableLiveData<State> = MutableLiveData()
    fun getStateLiveData() = currentState as LiveData<State>

    private val uiEffectsRelay = PublishRelay.create<UiEffect>()
    private val uiEffectsInput: Consumer<UiEffect> get() = uiEffectsRelay
    private val uiEffectsState: Observable<UiEffect> get() = uiEffectsRelay

    private val currentUiEffects: MutableLiveData<UiEffect> = MutableLiveData()
    fun getUiEffectsLiveData() = currentUiEffects as LiveData<UiEffect>

    private var posts: List<Post> = emptyList()

    fun onAttachViewModel() {
        state.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentState.value = it }
            .disposeOnFinish()
        uiEffectsState.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentUiEffects.value = it }
            .disposeOnFinish()

        input.accept(Action.LoadPosts(isNeedToScrollRecyclerToSavedPosition = true))  // show cached posts firstly.
        if (!isFilterByFavorites && isFirstLoading) {
            refreshPosts()  // silent update from network on 1st loading.
        }
    }

    fun setNeutralState() = input.accept(Action.ChangeFragment)

    fun refreshPosts() = input.accept(Action.LoadPosts(isLoading = false, isRefreshing = true))

    private fun loadPosts(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.LoadPosts::class.java)
                .flatMap { loadingAction ->
                    RepositoryProvider.postRepository
                        .fetchPosts(forceUpdate = loadingAction.isRefreshing, isFilterByFavorites = isFilterByFavorites)
                        .subscribeOn(Schedulers.io())
                        .toObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            posts = it
                            activateOnSuccessLoadingUiEffects(
                                isRefreshing = loadingAction.isRefreshing,
                                isNeedToScrollRecyclerToSavedPosition = loadingAction.isNeedToScrollRecyclerToSavedPosition
                            )
                            Action.PostsUpdated(posts = posts) as Action
                        }
                        .onErrorReturn { error ->
                            activateOnSuccessLoadingUiEffects(isRefreshing = loadingAction.isRefreshing)
                            Action.ErrorUpdatingPosts(error = error)
                        }
                }
        }
    }

    private fun activateOnSuccessLoadingUiEffects(isRefreshing: Boolean, isNeedToScrollRecyclerToSavedPosition: Boolean = false) {
        if (isRefreshing) {
            uiEffectsInput.accept(UiEffect.FinishRefreshing)
        }
        updateFavoritesVisibility()
        if (isNeedToScrollRecyclerToSavedPosition) {
            uiEffectsInput.accept(UiEffect.ScrollRecyclerToSavedPosition)
        }
    }

    /**
     * process user's clicking by like button
     */
    fun processLike(postIndex: Int) {
        val updatedPosts = posts.toMutableList()
        val likedPost = updatedPosts.likePostAtPosition(postIndex)
        if (isFilterByFavorites) {
            updatedPosts.removeAt(postIndex)
        }
        input.accept(Action.PostsUpdated(updatedPosts))

        processLikeRequest(updatedPosts, likedPost, postIndex)
    }

    private fun processLikeRequest(updatedPosts: MutableList<Post>, likedPost: Post, postIndex: Int) {
        RepositoryProvider.postRepository
            .sendLikeRequest(likedPost, likedPost.isLiked)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatedPost ->
                    logMessage("Success like request of post at position $postIndex")
                    posts = updatedPosts
                    if (updatedPost.likesCount != likedPost.likesCount && !isFilterByFavorites) {
                        (posts as MutableList<Post>)[postIndex] = updatedPost
                    }
                    input.accept(Action.PostsUpdated(posts))
                    updateFavoritesVisibility()
                },
                { throwable ->
                    logError("Fail to like post at $postIndex position", throwable)
                    updateOnErrorWithDelay(throwable)
                }
            )
            .disposeOnFinish()
    }

    private fun updateFavoritesVisibility() = uiEffectsInput.accept(UiEffect.UpdateFavoritesVisibility(posts.areLikedPostsPresent()))

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        val updatedPosts = posts.toMutableList()
        updatedPosts.removeAt(postIndex)
        input.accept(Action.PostsUpdated(updatedPosts))

        requestToHidePost(postIndex, updatedPosts)
    }

    private fun requestToHidePost(postIndex: Int, updatedPosts: List<Post>) {
        val postToHide: Post = posts[postIndex]
        RepositoryProvider.postRepository
            .hidePost(postToHide)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { responseCode ->
                    posts = updatedPosts
                    logMessage("post is hidden, response code: $responseCode")
                    input.accept(Action.PostsUpdated(posts))
                    updateFavoritesVisibility()
                },
                { throwable ->
                    logError("fail to hide post at $postIndex position", throwable)
                    updateOnErrorWithDelay(throwable)
                }
            )
            .disposeOnFinish()
    }

    private fun updateOnErrorWithDelay(throwable: Throwable) {
        Handler(Looper.getMainLooper()).postDelayed({
            input.accept(Action.ErrorUpdatingPosts(error = throwable, posts = posts))
            updateFavoritesVisibility()
        }, UPDATING_DELAY_MILLIS)
    }

    private fun logMessage(message: String) = Log.d(TAG, message)

    private fun logError(logErrorMessage: String, throwable: Throwable) = Log.e(TAG, logErrorMessage, throwable)

    class PostsViewModelFactory(
        private val isFilterByFavorites: Boolean,
        private val isFirstLoading: Boolean
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = PostsViewModel(isFilterByFavorites, isFirstLoading) as T
    }
}
