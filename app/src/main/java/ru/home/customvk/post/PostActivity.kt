package ru.home.customvk.post

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_post.*
import ru.home.customvk.R

class PostActivity : AppCompatActivity() {

    private val postViewModel by lazy { ViewModelProvider(this).get(PostViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val adapter = initAdapter()
        postViewModel.posts.observe(this) { adapter.posts = it }
        postViewModel.getPosts()
    }

    private fun initAdapter(): PostAdapter {
        val layoutManager = LinearLayoutManager(this)
        val adapter = PostAdapter(
            afterUpdateAction = { layoutManager.scrollToPosition(0) },
            onLikeListener = { itemPosition -> postViewModel.onLikePostAction(itemPosition) },
            onRemoveSwipeListener = { postId -> postViewModel.onRemovingSwipeAction(postId) }
        )
        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))

        ItemTouchHelper(ItemTouchHelperCallback(adapter as SwipeHelperAdapter))
            .attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener {
            postViewModel.refreshPosts()
            postsRefresher.post { postsRefresher.isRefreshing = false }
        }
        return adapter
    }

    private fun createPostsDivider(layoutManager: LinearLayoutManager): DividerItemDecoration {
        val divider = DividerItemDecoration(postsRecycler.context, layoutManager.orientation)
        divider.setDrawable(ShapeDrawable().apply {
            intrinsicHeight = resources.getDimensionPixelOffset(R.dimen.posts_divider_size)
            paint.color = Color.DKGRAY
        })
        return divider
    }
}

class PostViewModel : ViewModel() {
    val posts: MutableLiveData<MutableList<Post>> = MutableLiveData()

    private var isUpdated = false

    fun getPosts(): List<Post> {
        if (posts.value == null) {
            posts.value = PostUtils.initializationPosts
        }
        return posts.value!!
    }

    fun refreshPosts() {
        posts.value = PostUtils.updatedPosts
        isUpdated = true
    }

    fun onLikePostAction(postPosition: Int): Post {
        val post = posts.value!![postPosition]
        if (post.isFavorite) {
            post.isFavorite = false
            post.likesCount--
        } else {
            post.isFavorite = true
            post.likesCount++
        }
        if (isUpdated.not()) {  // in case there was not update - update likes in the same post in updated version.
            PostUtils.updateLikesInfo(post)
        }
        return post
    }

    fun onRemovingSwipeAction(postId: Int) {
        if (isUpdated.not()) {  // in case there was not update - remove the same post in updated version too.
            PostUtils.removePostFromUpdates(postId)
        }
    }
}