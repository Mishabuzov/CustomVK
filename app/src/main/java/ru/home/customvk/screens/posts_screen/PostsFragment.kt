package ru.home.customvk.screens.posts_screen

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

    private val postsViewModel: PostsViewModel by lazy(NONE) {
        ViewModelProvider(this, PostsViewModel.PostsViewModelFactory(isFavoritesFragment)).get(PostsViewModel::class.java)
    }

    private lateinit var adapter: PostAdapter
    private lateinit var layoutManager: LinearLayoutManager

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
        initObservers()
    }

    private fun initObservers() {
        postsViewModel.getPostsLiveData().observe(viewLifecycleOwner) { adapter.posts = it }
        postsViewModel.showErrorDialogAction.observe(viewLifecycleOwner) { showErrorDialog() }
        postsViewModel.onSynchronizationCompleteAction.observe(viewLifecycleOwner) { postsFragmentInterractor?.onSynchronizationComplete() }
        postsViewModel.finishUpdatingAction.observe(viewLifecycleOwner) {
            postsRefresher.isRefreshing = false
            postsRecycler.post { layoutManager.scrollToPosition(0) }
        }
        postsViewModel.updateFavoritesVisibilityAction.observe(viewLifecycleOwner) { isFavoritesFragmentVisible ->
            postsFragmentInterractor?.updateFavoritesVisibility(isFavoritesFragmentVisible)
        }
    }

    private fun showErrorDialog() = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
        .setTitle(R.string.posts_loading_dialog_error_title)
        .setMessage(R.string.posts_loading_dialog_error_message)
        .setPositiveButton(
            getString(android.R.string.ok)
        ) { dialog, _ ->
            dialog.cancel()
        }
        .create()
        .show()

    private fun configureLayout() {
        adapter = createAdapter()
        layoutManager = LinearLayoutManager(context)

        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))

        ItemTouchHelper(PostTouchHelperCallback(adapter)).attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener { postsViewModel.refreshPosts() }
    }

    private fun createAdapter() = PostAdapter(
        onLikeListener = { postIndex ->
            postsViewModel.processLike(postIndex)
            postsFragmentInterractor?.onChangesMade()
        },
        onRemoveSwipeListener = { postPosition ->
            postsViewModel.hidePost(postPosition)
            postsFragmentInterractor?.onChangesMade()
        }
    )

    private fun synchronizePostsIfNeeded() = postsFragmentInterractor?.isNeedToSyncPosts()?.let { isNeedToSync ->
        postsViewModel.synchronizePostsIfNeeded(isNeedToSync)
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
        fun updateFavoritesVisibility(isFavoritesFragmentVisible: Boolean)
        fun onChangesMade()
        fun isNeedToSyncPosts(): Boolean
        fun onSynchronizationComplete()
    }
}