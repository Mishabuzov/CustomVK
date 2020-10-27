package ru.home.customvk.screens.posts_screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.home.customvk.PostsProvider
import ru.home.customvk.PostsRepository
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
        fetchPosts()
    }

    private fun fetchPosts(isUpdatingAction: Boolean = false) {
        val postsDisposable = PostsProvider.postsRepository
            .fetchPosts(isFilterByFavorites = isFilterByFavorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                if (isUpdatingAction) {
                    finishUpdatingAction.call()
                }
            }
            .subscribe(
                { updatePostsAndCheckFavoritesVisibility(it) },
                { exception ->
                    Log.e(TAG, "fetching posts exception", exception)
                    showErrorDialogAction.call()
                }
            )
        compositeDisposable.add(postsDisposable)
    }

    private fun areLikedPostsPresent(): Boolean = posts.value?.let { it.filterByFavorites().count() > 0 } ?: false

    private fun updatePostsAndCheckFavoritesVisibility(updatedPosts: List<Post>) {
        posts.value = updatedPosts
        updateFavoritesVisibilityAction.value = areLikedPostsPresent()
    }

    /**
     * process user's clicking by like button
     */
    fun processLike(postIndex: Int) {
        val updatedPost = posts.value!![postIndex].copy()
        if (updatedPost.isLiked) {
            onDislikeAction(updatedPost, postIndex)
        } else {
            onPositiveLikeAction(updatedPost, postIndex)
        }
    }

    private fun PostsRepository.sendLikeRequest(post: Post, isPositiveLike: Boolean): Single<Int> =
        if (isPositiveLike) {
            likePost(post)
        } else {
            dislikePost(post)
        }

    private fun processLikeRequest(post: Post, postIndex: Int, isPositiveLikeRequest: Boolean = true) {
        val likeDisposable = PostsProvider.postsRepository
            .sendLikeRequest(post, isPositiveLikeRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatedLikesCount ->
                    Log.d(TAG, "success like request")
                    if (updatedLikesCount != post.likesCount && !isFilterByFavorites) {
                        val updatedPost = post.copy(likesCount = updatedLikesCount)
                        updatePost(updatedPost, postIndex)
                    }
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
        updatePostsAndCheckFavoritesVisibility(updatedPosts)
    }

    private fun onPositiveLikeAction(post: Post, postIndex: Int) {
        post.isLiked = true
        post.likesCount++
        updatePost(post, postIndex)
        processLikeRequest(post, postIndex)
    }

    private fun onDislikeAction(post: Post, postIndex: Int) {
        post.isLiked = false
        post.likesCount--
        if (isFilterByFavorites) {
            removePost(postIndex)  // in case of favorites' screen - remove on dislike is only possible option.
        } else {
            updatePost(post, postIndex)
        }
        processLikeRequest(post, postIndex, isPositiveLikeRequest = false)
    }

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        PostsProvider.postsRepository.rememberHiddenPost(posts.value!![postIndex])
        removePost(postIndex)
    }

    private fun removePost(postIndex: Int) {
        val updatedPosts = posts.value!!.toMutableList()
        updatedPosts.removeAt(postIndex)
        updatePostsAndCheckFavoritesVisibility(updatedPosts)
    }

    /**
     * Refresh newsfeed by SwipeRefresh action.
     */
    fun refreshPosts() = fetchPosts(isUpdatingAction = true)

    fun synchronizePostsIfNeeded(isNeedToSync: Boolean) {
        if (isNeedToSync && posts.value != null) {
            fetchPosts()
            onSynchronizationCompleteAction.call()
        }
    }

    override fun onCleared() = compositeDisposable.clear()

    class PostsViewModelFactory(private val isFilterByFavorites: Boolean) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = PostsViewModel(isFilterByFavorites) as T
    }
}