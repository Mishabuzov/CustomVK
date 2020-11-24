package ru.home.customvk.presentation.posts_screen

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
        private const val IS_FAVORITES_FRAGMENT_VISIBLE = "is_favorites_fragments_visible"

        fun createIntent(context: Context) = Intent(context, PostsActivity::class.java)
    }

    private lateinit var postsNewsFragment: PostsFragment
    private val postsFavoritesFragment by lazy(NONE) { PostsFragment.newInstance(isFavorite = true) }

    private var currentScreenItemId: Int = ScreenType.NEWS.screenItemId
    private var isFavoritesFragmentVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        if (savedInstanceState == null) {
            initPostsNewsFragments(isFirstLoading = true)
            replaceCurrentFragment(postsNewsFragment)
        } else {
            initPostsNewsFragments()
            currentScreenItemId = savedInstanceState.getInt(CURRENT_SCREEN_TYPE)
            val isFavoritesFragmentVisible = savedInstanceState.getBoolean(IS_FAVORITES_FRAGMENT_VISIBLE)
            updateFavoritesVisibility(isFavoritesFragmentVisible)
            replaceCurrentFragment(getFragmentByScreenId())
        }
        bottom_navigation.setOnNavigationItemSelectedListener {
            switchScreenIfNeeded(it.itemId)
            true
        }
    }

    private fun initPostsNewsFragments(isFirstLoading: Boolean = false) {
        postsNewsFragment = PostsFragment.newInstance(isFirstLoading = isFirstLoading)
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

    private fun getFragmentByScreenId(): PostsFragment {
        return when (currentScreenItemId) {
            ScreenType.NEWS.screenItemId -> postsNewsFragment
            ScreenType.FAVORITES.screenItemId -> postsFavoritesFragment
            else -> throw IllegalArgumentException("Fragment with detected id is not yet implemented")
        }
    }

    private fun switchScreenIfNeeded(screenItemId: Int) {
        if (currentScreenItemId != screenItemId) {
            getFragmentByScreenId().setNeutralStateBeforeChangingFragment()
            setFragmentByScreenId(screenItemId)
        }
    }

    private fun replaceCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_SCREEN_TYPE, currentScreenItemId)
        outState.putBoolean(IS_FAVORITES_FRAGMENT_VISIBLE, isFavoritesFragmentVisible)
    }

    private enum class ScreenType(val screenItemId: Int) {
        NEWS(R.id.actionNews),
        FAVORITES(R.id.actionFavorites)
    }
}
