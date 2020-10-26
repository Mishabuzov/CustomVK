package ru.home.customvk.posts_screen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.home.customvk.Post
import ru.home.customvk.PostsProvider

open class PostsViewModel(private val isFilterByFavorites: Boolean, private val dialogErrorAction: () -> Unit) : ViewModel() {

    private companion object {
        private const val TAG = "POSTS_VIEW_MODEL"
    }

    val posts: MutableLiveData<List<Post>> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        val postsDisposable = PostsProvider.postsRepository
            .fetchPosts(isFilterByFavorites = isFilterByFavorites)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ newPosts ->
                posts.value = newPosts
            }, { exception ->
                Log.w(TAG, exception)
                dialogErrorAction()
            })
        compositeDisposable.add(postsDisposable)
    }

    /**
     * process user's clicking by like button
     */
    fun processLike(postIndex: Int) {
        val updatedPost = posts.value!![postIndex].copy()
        if (updatedPost.isFavorite) {
            onDislikeAction(updatedPost)
        } else {
            onPositiveLikeAction(updatedPost)
        }
        if (isFilterByFavorites) {
            removePost(postIndex)  // in case of favorites' screen - remove on dislike is only possible option.
        } else {
            updatePost(updatedPost, postIndex)
        }
        val likeDisposable = PostsProvider.postsRepository
            .likePost(updatedPost)
            .subscribe({
                Log.d(TAG, "success like")
            }, { exception ->
                Log.w(TAG, exception)
            })
        compositeDisposable.add(likeDisposable)
    }

    private fun updatePost(updatedPost: Post, postIndex: Int) {
        val updatedPosts = posts.value!!.toMutableList()
        updatedPosts[postIndex] = updatedPost
        posts.value = updatedPosts
    }

    private fun onPositiveLikeAction(post: Post) {
        post.isFavorite = true
        post.likesCount++
    }

    private fun onDislikeAction(post: Post) {
        post.isFavorite = false
        post.likesCount--
    }

    /**
     * process hiding of post by swiping from right to left in the newsfeed.
     */
    fun hidePost(postIndex: Int) {
        PostsProvider.postsRepository.saveHiddenPostId(posts.value!![postIndex].id)
        removePost(postIndex)
    }

    private fun removePost(postIndex: Int) {
        val updatedPosts = posts.value!!.toMutableList()
        updatedPosts.removeAt(postIndex)
        posts.value = updatedPosts
    }

    /**
     * Refresh newsfeed by SwipeRefresh action.
     */
    fun refreshPosts() {
        PostsProvider.postsRepository.notifyAboutUpdatingAction()
        fetchPosts()
    }

    fun areLikedPostsPresent(): Boolean = posts.value?.let { it.filter(Post::isFavorite).count() > 0 } ?: false

    override fun onCleared() = compositeDisposable.clear()

    class PostsViewModelFactory(private val isFilterByFavorites: Boolean, private val showErrorDialog: () -> Unit) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            PostsViewModel(isFilterByFavorites, showErrorDialog) as T
    }
}