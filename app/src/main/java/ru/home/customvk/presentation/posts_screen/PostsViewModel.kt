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
import ru.home.customvk.utils.PostUtils.setDislikedAndDecreaseLikesCount
import ru.home.customvk.utils.PostUtils.setLikedAndIncreaseLikesCount

private typealias PostSideEffect = SideEffect<State, out Action>

open class PostsViewModel(private val isFilterByFavorites: Boolean) : BaseRxViewModel() {

    private companion object {
        private const val TAG = "POSTS_VIEW_MODEL"
        private const val DELAY_BEFORE_CANCELING_ACTION_MILLIS = 500L
    }

    private var posts: MutableList<Post>? = emptyList<Post>().toMutableList()

    private val inputRelay: Relay<Action> = PublishRelay.create()
    val input: Consumer<Action> get() = inputRelay

    private val currentState: MutableLiveData<State> = MutableLiveData()
    fun getStateLiveData() = currentState as LiveData<State>

    private val state: Observable<State> = inputRelay.reduxStore(
        initialState = State(),
        sideEffects = listOf(loadFirstPage(), synchronizePosts(), refreshPosts(), likePost(), requestToHidePost()),
        reducer = { state, action -> state.reduce(action) }
    )

    init {
        state.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentState.value = it }
            .disposeOnFinish()

        input.accept(Action.LoadFirstPage)
    }

    private fun loadFirstPage(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.LoadFirstPage::class.java)
                .flatMap {
                    if (isFilterByFavorites) {
                        fetchPosts()
                    } else {
                        // since "news" tab is initialized immediately after authentication -> it should perform network query
                        fetchPosts(forceUpdate = true)
                    }.map { Action.PostsLoaded(posts = it) as Action }
                }.setupDefaultOnErrorLoadingPostsAction()
        }
    }

    private fun fetchPosts(forceUpdate: Boolean = false): Observable<List<Post>> {
        return RepositoryProvider.postRepository
            .fetchPosts(forceUpdate = forceUpdate, isFilterByFavorites = isFilterByFavorites)
            .subscribeOn(Schedulers.io())
            .doOnSuccess(::setPostsValue)
            .toObservable()
    }

    private fun logMessage(message: String) = Log.d(TAG, message)

    private fun logError(logErrorMessage: String, throwable: Throwable) = Log.e(TAG, logErrorMessage, throwable)

    private fun setPostsValue(updatedPosts: List<Post>) {
        posts = updatedPosts.toMutableList()
    }

    /**
     * process user's clicking by like button
     */
    fun processLike(postIndex: Int) {
        val postToUpdate = posts!![postIndex].copy()
        if (postToUpdate.isLiked) {
            onDislikeAction(postToUpdate, postIndex)
        } else {
            onPositiveLikeAction(postToUpdate, postIndex)
        }
    }

    private fun onPositiveLikeAction(post: Post, postIndex: Int) {
        post.setLikedAndIncreaseLikesCount()
        updatePost(postIndex, post)
        input.accept(Action.UpdatePostsLocally(posts!!))
        input.accept(Action.LikePost(postIndex))
    }

    private fun onDislikeAction(post: Post, postIndex: Int) {
        post.setDislikedAndDecreaseLikesCount()
        if (isFilterByFavorites) {
            removePost(postIndex)  // in case of favorites' screen - remove on dislike is only possible option.
        } else {
            updatePost(postIndex, post)
        }
        input.accept(Action.UpdatePostsLocally(posts!!))
        input.accept(Action.LikePost(postIndex, isPositiveLike = false))
    }

    private fun likePost(): PostSideEffect {
        return { actions, state ->
            actions.ofType(Action.LikePost::class.java)
                .flatMap {
                    val positionOfLikedPost = state().likeStateInfo!!.positionOfPostToLike
                    val likedPost: Post = posts?.get(positionOfLikedPost)!!
                    val isPositiveLikeRequest = state().likeStateInfo!!.isPositiveLike
                    RepositoryProvider.postRepository
                        .sendLikeRequest(likedPost, isPositiveLikeRequest)
                        .subscribeOn(Schedulers.io())
                        .doOnSuccess { updatedPost ->
                            logMessage("Success like request of post at position $positionOfLikedPost")
                            if (updatedPost.likesCount != likedPost.likesCount && !isFilterByFavorites) {
                                updatePost(positionOfLikedPost, updatedPost)
                            }
                        }
                        .toObservable()
                        .map { Action.PostIsLiked(posts = posts!!) as Action }
                        .onErrorReturn { throwable ->
                            Handler(Looper.getMainLooper()).postDelayed({
                                cancelLikeRequest(likedPost.copy(), positionOfLikedPost, isPositiveLikeRequest)
                                logError("Fail to like post at $positionOfLikedPost position", throwable)
                            }, DELAY_BEFORE_CANCELING_ACTION_MILLIS)
                            Action.ErrorLikePosts(error = throwable, posts = posts!!)
                        }
                }
        }
    }

    private fun cancelLikeRequest(post: Post, postIndex: Int, wasPositiveLikeRequest: Boolean) {
        if (wasPositiveLikeRequest) {
            post.setDislikedAndDecreaseLikesCount()
            updatePost(postIndex, post)
        } else {
            post.setLikedAndIncreaseLikesCount()
            if (isFilterByFavorites) {
                addPost(postIndex, post)
            } else {
                updatePost(postIndex, post)
            }
        }
    }

    private fun addPost(postIndex: Int, post: Post) {
        posts?.add(postIndex, post)
    }

    private fun updatePost(postIndex: Int, updatedPost: Post) {
        posts!![postIndex] = updatedPost
    }

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        removePost(postIndex)
        input.accept(Action.UpdatePostsLocally(posts!!))

        input.accept(Action.HidePost(postIndex))
    }

    private fun requestToHidePost(): PostSideEffect {
        return { actions, state ->
            actions.ofType(Action.HidePost::class.java)
                .flatMap {
                    val postIndex = state().indexOfPostToHide!!
                    val postToHide: Post = posts!![postIndex]
                    RepositoryProvider.postRepository
                        .hidePost(postToHide)
                        .subscribeOn(Schedulers.io())
                        .doOnSuccess { responseCode -> logMessage("post is hidden, response code: $responseCode") }
                        .toObservable()
                        .map { Action.PostIsHidden(posts!!) as Action }
                        .onErrorReturn { throwable ->
                            Handler(Looper.getMainLooper()).postDelayed({
                                logError("fail to hide post at $postIndex position", throwable)
                                addPost(postIndex, postToHide)
                            }, DELAY_BEFORE_CANCELING_ACTION_MILLIS)
                            Action.ErrorHidePosts(throwable, posts!!)
                        }
                }
        }
    }

    /**
     * Post have to be removed in case of successful hiding or dislike in "favorites" tab.
     */
    private fun removePost(postIndex: Int) {
        posts?.removeAt(postIndex)
    }

    /**
     * Refresh newsfeed by SwipeRefresh action.
     */
    private fun refreshPosts(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.RefreshPosts::class.java)
                .flatMap { fetchPosts(forceUpdate = true).map { Action.FinishRefreshing(it) as Action } }
                .setupDefaultOnErrorLoadingPostsAction()
        }
    }

    fun synchronizePostsIfNeeded(isNeedToSync: Boolean) {
        if (isNeedToSync && posts != null) {
            input.accept(Action.SynchronizePosts)
        }
    }

    private fun synchronizePosts(): PostSideEffect {
        return { actions, _ ->
            actions.ofType(Action.SynchronizePosts::class.java)
                .flatMap { fetchPosts().map { Action.FinishSynchronization(it) as Action } }
                .setupDefaultOnErrorLoadingPostsAction()
        }
    }

    class PostsViewModelFactory(private val isFilterByFavorites: Boolean) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = PostsViewModel(isFilterByFavorites) as T
    }
}