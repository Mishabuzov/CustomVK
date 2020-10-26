package ru.home.customvk.posts_screen

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.news_fragment.*
import ru.home.customvk.R
import kotlin.LazyThreadSafetyMode.NONE

class PostsFragment : Fragment() {

    companion object {
        private const val ARG_FAVORITE = "is_favorite"

        fun newInstance(isFavorite: Boolean = false): PostsFragment =
            PostsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_FAVORITE, isFavorite)
                }
            }
    }

    private var isViewModelInitialized = false

    private val postsViewModel: PostsViewModel by lazy(NONE) {
        isViewModelInitialized = true
        ViewModelProvider(this, PostsViewModel.PostsViewModelFactory(isFavoritesFragment) { showAlertDialog() }).get(
            PostsViewModel::class.java
        )
    }

    private lateinit var adapter: PostAdapter

    private var isFavoritesFragment = false

    private var postsFragmentInterractor: PostsFragmentInterractor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.news_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFavoritesFragment = requireArguments().getBoolean(ARG_FAVORITE)
        if (context is PostsFragmentInterractor) {
            postsFragmentInterractor = (context as PostsFragmentInterractor)
        }
        configureLayout()
        synchronizePostsIfNeeded()
        postsViewModel.posts.observe(viewLifecycleOwner) {
            adapter.posts = it
        }
    }

    fun showAlertDialog() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage(getString(R.string.dialog_error))
        alertDialogBuilder.setCancelable(true)

        alertDialogBuilder.setPositiveButton(
            getString(android.R.string.ok)
        ) { dialog, _ ->
            dialog.cancel()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun configureLayout() {
        adapter = createAdapter()
        val layoutManager = LinearLayoutManager(context)

        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))

        ItemTouchHelper(PostTouchHelperCallback(adapter)).attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener {
            postsViewModel.refreshPosts()
            onAffectedFavoritesChanges()
            postsRefresher.post {
                postsRefresher.isRefreshing = false
                layoutManager.scrollToPosition(0)
            }
        }
    }

    private fun createAdapter(): PostAdapter =
        PostAdapter(
            onLikeListener = { postIndex ->
                postsViewModel.processLike(postIndex)
                onAffectedFavoritesChanges()
            },
            onRemoveSwipeListener = { postPosition ->
                postsViewModel.hidePost(postPosition)
                onAffectedFavoritesChanges()
            }
        )

    private fun onAffectedFavoritesChanges() {
        checkFavoritesVisibility()
        postsFragmentInterractor?.onChangesMade()
    }

    private fun checkFavoritesVisibility() =
        postsFragmentInterractor?.checkFavoritesVisibility(postsViewModel.areLikedPostsPresent())

    private fun synchronizePostsIfNeeded() =
        postsFragmentInterractor?.isNeedToSyncPosts()?.let { isNeedToSync ->
            if (isNeedToSync && isViewModelInitialized) {
                postsViewModel.fetchPosts()
                postsFragmentInterractor?.onSynchronizationComplete()
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

    interface PostsFragmentInterractor {
        fun checkFavoritesVisibility(isFavoritesFragmentVisible: Boolean)
        fun onChangesMade()
        fun isNeedToSyncPosts(): Boolean
        fun onSynchronizationComplete()
    }
}

