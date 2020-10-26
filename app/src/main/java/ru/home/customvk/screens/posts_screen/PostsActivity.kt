package ru.home.customvk.screens.posts_screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_news.*
import ru.home.customvk.R
import kotlin.LazyThreadSafetyMode.NONE

class PostsActivity : AppCompatActivity(), PostsFragment.PostsFragmentInterractor {

    companion object {
        private const val CURRENT_SCREEN_TYPE = "screen_type"
        private const val IS_NEED_TO_SYNC_POSTS = "is_need_to_sync_posts"
        private const val IS_FAVORITES_FRAGMENT_VISIBLE = "is_favorites_fragments_visible"

        fun createIntent(context: Context) = Intent(context, PostsActivity::class.java)
    }

    private val postsNewsFragment by lazy(NONE) { PostsFragment.newInstance() }
    private val postsFavoritesFragment by lazy(NONE) { PostsFragment.newInstance(isFavorite = true) }

    private var isNeedToSyncPosts = false
    private var currentScreenItemId: Int = ScreenType.NEWS.screenItemId
    private var isFavoritesFragmentVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        if (savedInstanceState == null) {
            replaceCurrentFragment(postsNewsFragment)
        } else {
            currentScreenItemId = savedInstanceState.getInt(CURRENT_SCREEN_TYPE)
            isNeedToSyncPosts = savedInstanceState.getBoolean(IS_NEED_TO_SYNC_POSTS)
            val isFavoritesFragmentVisible = savedInstanceState.getBoolean(IS_FAVORITES_FRAGMENT_VISIBLE)
            updateFavoritesVisibility(isFavoritesFragmentVisible)
        }
        bottom_navigation.setOnNavigationItemSelectedListener {
            switchScreenIfNeeded(it.itemId)
            true
        }
    }

    /**
     * Remembers current screenItemId, and setups fragment depending on it.
     */
    private fun setFragmentByScreenId(screenItemId: Int) {
        var fragmentToLoad: PostsFragment = postsNewsFragment
        when (screenItemId) {
            ScreenType.NEWS.screenItemId -> {
                currentScreenItemId = ScreenType.NEWS.screenItemId
            }
            ScreenType.FAVORITES.screenItemId -> {
                currentScreenItemId = ScreenType.FAVORITES.screenItemId
                fragmentToLoad = postsFavoritesFragment
            }
        }
        replaceCurrentFragment(fragmentToLoad)
    }

    private fun switchScreenIfNeeded(screenItemId: Int) {
        if (currentScreenItemId != screenItemId) {
            setFragmentByScreenId(screenItemId)
        }
    }

    private fun replaceCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

    override fun updateFavoritesVisibility(isFavoritesFragmentVisible: Boolean) {
        if (isFavoritesFragmentVisible != this.isFavoritesFragmentVisible) {
            this.isFavoritesFragmentVisible = isFavoritesFragmentVisible

            bottom_navigation.menu.findItem(ScreenType.FAVORITES.screenItemId).isVisible = isFavoritesFragmentVisible
            if (!isFavoritesFragmentVisible) {
                bottom_navigation.menu.findItem(ScreenType.NEWS.screenItemId).isChecked = true
                switchScreenIfNeeded(ScreenType.NEWS.screenItemId)
            }
        }
    }

    override fun isNeedToSyncPosts(): Boolean = isNeedToSyncPosts

    override fun onChangesMade() {
        isNeedToSyncPosts = true
    }

    override fun onSynchronizationComplete() {
        isNeedToSyncPosts = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_SCREEN_TYPE, currentScreenItemId)
        outState.putBoolean(IS_NEED_TO_SYNC_POSTS, isNeedToSyncPosts)
        outState.putBoolean(IS_FAVORITES_FRAGMENT_VISIBLE, isFavoritesFragmentVisible)
    }

    private enum class ScreenType(val screenItemId: Int) {
        NEWS(R.id.actionNews),
        FAVORITES(R.id.actionFavorites)
    }
}