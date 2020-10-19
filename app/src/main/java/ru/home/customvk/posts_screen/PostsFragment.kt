package ru.home.customvk.posts_screen

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.news_fragment.*
import ru.home.customvk.Post
import ru.home.customvk.R

class PostsFragment : Fragment(), PostsActivityCallback {

    companion object {
        private const val ARG_FAVORITE = "is_favorite"

        fun newInstance(isFavorite: Boolean = false): PostsFragment {
            val fragment = PostsFragment()
            fragment.arguments = Bundle().apply { putBoolean(ARG_FAVORITE, isFavorite) }
            return fragment
        }
    }

    private val postViewModel by lazy { ViewModelProvider(this).get(PostsFragmentViewModel::class.java) }

    private val fragmentCallback by lazy { context as PostsFragmentCallback }

    private val adapter by lazy { createAdapter() }

    private val layoutManager by lazy { LinearLayoutManager(context) }

    private var isFavoritesFragment = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isFavoritesFragment = requireArguments().getBoolean(ARG_FAVORITE)
        postViewModel.isFilterByFavorites = isFavoritesFragment
        postViewModel.initPosts()
        return inflater.inflate(R.layout.news_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureLayout()
        postViewModel.posts.observe(viewLifecycleOwner) { adapter.posts = it }
    }

    private fun createAdapter(): PostAdapter {
        val onLikeAction: (Post) -> Unit = { post ->
            postViewModel.onLikePostAction(post)
            fragmentCallback.onLikeAction(post, isFavoritesFragment)
        }
        return PostAdapter(
            onLikeListener = onLikeAction,
            onRemoveSwipeListener = { postId -> postViewModel.onRemovingSwipeAction(postId) }
        )
    }

    private fun configureLayout() {
        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))

        ItemTouchHelper(PostTouchHelperCallback(adapter)).attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener {
            postViewModel.refreshPosts()
            postsRefresher.post {
                postsRefresher.isRefreshing = false
                layoutManager.scrollToPosition(0)
            }
        }
    }

    private fun createPostsDivider(layoutManager: LinearLayoutManager): DividerItemDecoration {
        val divider = DividerItemDecoration(postsRecycler.context, layoutManager.orientation)
        divider.setDrawable(ShapeDrawable().apply {
            intrinsicHeight = resources.getDimensionPixelOffset(R.dimen.posts_divider_size)
            paint.color = Color.DKGRAY
        })
        return divider
    }

    override fun likePostInFeed(post: Post) {
        val index = postViewModel.likePostInFeed(post)
        adapter.notifyItemChanged(index)
    }

}