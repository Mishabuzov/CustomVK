package ru.home.customvk.presentation.posts_screen

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
import ru.home.customvk.utils.PostUtils.likePostAtPosition
import java.util.concurrent.TimeUnit

private typealias PostSideEffect = SideEffect<State, out Action>

class PostsViewModel(private val isFilterByFavorites: Boolean) : BaseRxViewModel() {

    private companion object {
        private const val TAG = "POSTS_VIEW_MODEL"

        // this is a small delay to let recyclerView finish animation of previous post's updating & avoid possible inconsistency error.
        private const val UPDATING_DELAY_MILLIS = 500L
    }

    private val inputRelay: Relay<Action> = PublishRelay.create()
    private val input: Consumer<Action> get() = inputRelay
    private var state: Observable<State> = inputRelay.reduxStore(
        initialState = State(),
        sideEffects = listOf(loadPosts()),
        reducer = { state, action -> state.reduce(action) }
    ).distinctUntilChanged()

    private val currentState: MutableLiveData<State> = MutableLiveData()
    fun getStateLiveData() = currentState as LiveData<State>

    private var posts: List<Post> = emptyList()

    fun onAttachViewModel() {
        state.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentState.value = it }
            .disposeOnFinish()

        if (posts.isEmpty()) {
            input.accept(Action.LoadPosts())  // show cached posts firstly
            if (!isFilterByFavorites) {
                refreshPosts(isNeedToSyncAfterUpdate = false)  // silent update, don't sync because of 1st loading.
            }
        }
    }

    fun setNeutralState() =
        input.accept(Action.PostsCleared)

    fun refreshPosts(isNeedToSyncAfterUpdate: Boolean = true) {
        input.accept(
            Action.LoadPosts(
                isLoading = false,
                isRefreshing = true,
                isNeedToSyncAfterUpdate = isNeedToSyncAfterUpdate,
                isUpdatingFavoritesVisibility = true
            )
        )
    }

    fun synchronizePostsIfNeeded(isNeedToSync: Boolean) {
        if (isNeedToSync) {
            input.accept(Action.LoadPosts(isLoading = false, isSyncStarted = true))
        } else {
            input.accept(Action.PostsUpdated(posts = posts))
        }
    }

    private fun loadPosts(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.LoadPosts::class.java)
                .distinctUntilChanged()
                .switchMap { loadingAction ->
                    RepositoryProvider.postRepository
                        .fetchPosts(forceUpdate = loadingAction.isRefreshing, isFilterByFavorites = isFilterByFavorites)
                        .subscribeOn(Schedulers.io())
                        .toObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            posts = it
                            Action.PostsUpdated(
                                posts = posts,
                                isNeedToSyncAfterUpdate = loadingAction.isNeedToSyncAfterUpdate,
                                isUpdatingFavoritesVisibility = loadingAction.isUpdatingFavoritesVisibility,
                                isSyncCompleted = loadingAction.isSyncStarted,
                                isRefreshing = loadingAction.isRefreshing
                            ) as Action
                        }
                }
                .onErrorReturn { error -> Action.ErrorUpdatingPosts(error) }
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
        input.accept(Action.PostsUpdated(updatedPosts, isNeedToSyncAfterUpdate = true, isUpdatingFavoritesVisibility = true))

        processLikeRequest(updatedPosts, likedPost, postIndex)
    }

    private fun processLikeRequest(updatedPosts: MutableList<Post>, likedPost: Post, postIndex: Int) {
        RepositoryProvider.postRepository
            .sendLikeRequest(likedPost, likedPost.isLiked)
            .delay(UPDATING_DELAY_MILLIS, TimeUnit.MILLISECONDS, Schedulers.io(), true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { updatedPost ->
                logMessage("Success like request of post at position $postIndex")
                posts = updatedPosts
                if (updatedPost.likesCount != likedPost.likesCount && !isFilterByFavorites) {
                    (posts as MutableList<Post>)[postIndex] = updatedPost
                    input.accept(Action.PostsUpdated(posts, isNeedToSyncAfterUpdate = true))
                }
            }
            .doOnError { throwable ->
                logError("Fail to like post at $postIndex position", throwable)
                input.accept(Action.ErrorUpdatingPosts(error = throwable, posts = posts))
            }
            .subscribe()
            .disposeOnFinish()
    }

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        val updatedPosts = posts.toMutableList()
        updatedPosts.removeAt(postIndex)
        input.accept(Action.PostsUpdated(updatedPosts, isNeedToSyncAfterUpdate = true, isUpdatingFavoritesVisibility = true))

        requestToHidePost(postIndex, updatedPosts)
    }

    private fun requestToHidePost(postIndex: Int, updatedPosts: List<Post>) {
        val postToHide: Post = posts[postIndex]
        RepositoryProvider.postRepository
            .hidePost(postToHide)
            .delay(UPDATING_DELAY_MILLIS, TimeUnit.MILLISECONDS, Schedulers.io(), true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { responseCode ->
                posts = updatedPosts
                logMessage("post is hidden, response code: $responseCode")
            }
            .doOnError { throwable ->
                logError("fail to hide post at $postIndex position", throwable)
                input.accept(Action.ErrorUpdatingPosts(throwable, posts))
            }
            .subscribe()
            .disposeOnFinish()
    }

    private fun logMessage(message: String) = Log.d(TAG, message)

    private fun logError(logErrorMessage: String, throwable: Throwable) = Log.e(TAG, logErrorMessage, throwable)

    class PostsViewModelFactory(private val isFilterByFavorites: Boolean) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = PostsViewModel(isFilterByFavorites) as T
    }
}