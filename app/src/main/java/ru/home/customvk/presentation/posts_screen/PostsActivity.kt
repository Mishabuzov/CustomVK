package ru.home.customvk.presentation.posts_screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_posts.*
import ru.home.customvk.R

class PostsActivity : AppCompatActivity(R.layout.activity_posts), PostsFragment.PostsFragmentInterractor {

    companion object {
        private const val CURRENT_FRAGMENT_TAG_KEY = "current_fragment_tag"
        private const val CURRENT_SCREEN_ITEM_ID_KEY = "current_screen_item_id"
        private const val IS_FAVORITES_FRAGMENT_VISIBLE_KEY = "is_favorites_fragments_visible"

        fun createIntent(context: Context) = Intent(context, PostsActivity::class.java)
    }

    private var currentFragment: Fragment = initNewsFeedFragment(isFirstLoading = true)
    private var currentFragmentTag: String = ScreenType.NEWS.fragmentTag
    private var currentScreenItemId: Int = ScreenType.NEWS.screenItemId

    private var isFavoritesFragmentVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            setupCurrentFragment(currentFragment, currentFragmentTag)
        } else {
            currentScreenItemId = savedInstanceState.getInt(CURRENT_SCREEN_ITEM_ID_KEY, ScreenType.NEWS.screenItemId)
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG_KEY, ScreenType.NEWS.fragmentTag)
            val isFavoritesFragmentVisible = savedInstanceState.getBoolean(IS_FAVORITES_FRAGMENT_VISIBLE_KEY)
            updateFavoritesVisibility(isFavoritesFragmentVisible)
            currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag) as Fragment
        }
        bottom_navigation.setOnNavigationItemSelectedListener {
            switchScreenIfNeeded(it.itemId)
            true
        }
    }

    private fun initNewsFeedFragment(isFirstLoading: Boolean = false) = PostsFragment.newInstance(isFirstLoading = isFirstLoading)

    private fun initFavoritesFragment() = PostsFragment.newInstance(isFavorite = true)

    private fun setupCurrentFragment(fragment: Fragment, fragmentTag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, fragmentTag)
            .commit()
    }

    /**
     * Remembers current fragment, its tag and screenItemId, after that setups corresponding fragment.
     */
    private fun setFragmentByScreenId(screenItemId: Int) {
        when (screenItemId) {
            ScreenType.NEWS.screenItemId -> {
                currentScreenItemId = ScreenType.NEWS.screenItemId
                currentFragmentTag = ScreenType.NEWS.fragmentTag
                currentFragment = initNewsFeedFragment()
            }
            ScreenType.FAVORITES.screenItemId -> {
                currentScreenItemId = ScreenType.FAVORITES.screenItemId
                currentFragmentTag = ScreenType.FAVORITES.fragmentTag
                currentFragment = initFavoritesFragment()
            }
        }
        setupCurrentFragment(currentFragment, currentFragmentTag)
    }

    private fun switchScreenIfNeeded(screenItemId: Int) {
        if (currentScreenItemId != screenItemId) {
            setFragmentByScreenId(screenItemId)
        }
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
        outState.putInt(CURRENT_SCREEN_ITEM_ID_KEY, currentScreenItemId)
        outState.putString(CURRENT_FRAGMENT_TAG_KEY, currentFragmentTag)
        outState.putBoolean(IS_FAVORITES_FRAGMENT_VISIBLE_KEY, isFavoritesFragmentVisible)
    }

    private enum class ScreenType(val screenItemId: Int, val fragmentTag: String) {
        NEWS(R.id.actionNews, "news_fragment"),
        FAVORITES(R.id.actionFavorites, "favorites_fragment")
    }
}
