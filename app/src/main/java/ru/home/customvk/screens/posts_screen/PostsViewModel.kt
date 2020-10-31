package ru.home.customvk.screens.posts_screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.home.customvk.RepositoryProvider
import ru.home.customvk.models.local.Post
import ru.home.customvk.utils.PostUtils.filterByFavorites
import ru.home.customvk.utils.SingleLiveEvent

open class PostsViewModel(private val isFilterByFavorites: Boolean) : ViewModel() {

    private companion object {
        private const val TAG = "POSTS_VIEW_MODEL"
    }

    private val posts: MutableLiveData<List<Post>> = MutableLiveData()
    fun getPostsLiveData() = posts as LiveData<List<Post>>

    val showErrorDialogAction: SingleLiveEvent<Void> = SingleLiveEvent()

    val onSynchronizationCompleteAction: SingleLiveEvent<Void> = SingleLiveEvent()

    val finishUpdatingAction: SingleLiveEvent<Void> = SingleLiveEvent()

    val updateFavoritesVisibilityAction: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private val compositeDisposable = CompositeDisposable()

    init {
        fetchPostsQuery()
    }

    private fun onSuccessfulFetchingPosts(newPosts: List<Post>) {
        setupPostsValue(newPosts)
        checkFavoritesVisibility()
    }

    private fun onErrorFetchingPosts(errorMessage: String, throwable: Throwable) {
        Log.e(TAG, errorMessage, throwable)
        showErrorDialogAction.call()
    }

    private fun fetchPostsFromInternet(isUpdatingAction: Boolean = false) {
        val fetchingPostsFromInternetDisposable = RepositoryProvider.postRepository
            .fetchPostsFromInternet(isFilterByFavorites = isFilterByFavorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                if (isUpdatingAction) {
                    finishUpdatingAction.call()
                }
            }
            .subscribe(
                { newPosts ->
                    onSuccessfulFetchingPosts(newPosts)
                },
                { throwable ->
                    onErrorFetchingPosts("exception in fetching posts from internet", throwable)
                }
            )
        compositeDisposable.add(fetchingPostsFromInternetDisposable)
    }

    private fun fetchPostsQuery() {
        val loadPostsDisposable = RepositoryProvider.postRepository
            .loadPostsFromDatabase(isFilterByFavorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { newPosts ->
                    if (newPosts.isNotEmpty()) {
                        onSuccessfulFetchingPosts(newPosts)
                    } else {
                        fetchPostsFromInternet()
                    }
                },
                { throwable -> onErrorFetchingPosts("Exception in fetching posts from the database", throwable) }
            )
        compositeDisposable.add(loadPostsDisposable)
    }

    private fun areLikedPostsPresent(): Boolean = posts.value?.let { it.filterByFavorites().count() > 0 } ?: false

    private fun setupPostsValue(updatedPosts: List<Post>) {
        posts.value = updatedPosts
    }

    private fun checkFavoritesVisibility() {
        updateFavoritesVisibilityAction.value = areLikedPostsPresent()
    }

    /**
     * process user's clicking by like button
     */
    fun processLike(postIndex: Int) {
        val postToUpdate = posts.value!![postIndex].copy()
        if (postToUpdate.isLiked) {
            onDislikeAction(postToUpdate, postIndex)
        } else {
            onPositiveLikeAction(postToUpdate, postIndex)
        }
    }

    private fun processLikeQuery(post: Post, postIndex: Int, isPositiveLikeRequest: Boolean = true) {
        val likeDisposable = RepositoryProvider.postRepository
            .sendLikeRequest(post, isPositiveLikeRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatedPost ->
                    Log.d(TAG, "success like request")
                    if (updatedPost.likesCount != post.likesCount && !isFilterByFavorites) {
                        updatePost(updatedPost, postIndex)
                    }
                    checkFavoritesVisibility()
                },
                { exception ->
                    Log.e(TAG, "fail to like post at $postIndex position", exception)
                    showErrorDialogAction.call()
                }
            )
        compositeDisposable.add(likeDisposable)
    }

    private fun updatePost(updatedPost: Post, postIndex: Int) {
        val updatedPosts = posts.value!!.toMutableList()
        updatedPosts[postIndex] = updatedPost
        setupPostsValue(updatedPosts)
    }

    private fun onPositiveLikeAction(post: Post, postIndex: Int) {
        post.isLiked = true
        post.likesCount++
        updatePost(post, postIndex)
        processLikeQuery(post, postIndex)
    }

    private fun onDislikeAction(post: Post, postIndex: Int) {
        post.isLiked = false
        post.likesCount--
        if (isFilterByFavorites) {
            removePost(postIndex)  // in case of favorites' screen - remove on dislike is only possible option.
        } else {
            updatePost(post, postIndex)
        }
        processLikeQuery(post, postIndex, isPositiveLikeRequest = false)
    }

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        RepositoryProvider.postRepository.rememberHiddenPost(posts.value!![postIndex])
        removePost(postIndex)
        checkFavoritesVisibility()
    }

    private fun removePost(postIndex: Int) {
        val updatedPosts = posts.value!!.toMutableList()
        updatedPosts.removeAt(postIndex)
        setupPostsValue(updatedPosts)
    }

    /**
     * Refresh newsfeed by SwipeRefresh action.
     */
    fun refreshPosts() = fetchPostsFromInternet(isUpdatingAction = true)

    fun synchronizePostsIfNeeded(isNeedToSync: Boolean) {
        if (isNeedToSync && posts.value != null) {
            fetchPostsQuery()
            onSynchronizationCompleteAction.call()
        }
    }

    override fun onCleared() = compositeDisposable.clear()

    class PostsViewModelFactory(private val isFilterByFavorites: Boolean) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = PostsViewModel(isFilterByFavorites) as T
    }
}