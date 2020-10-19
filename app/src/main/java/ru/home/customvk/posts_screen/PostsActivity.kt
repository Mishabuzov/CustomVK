package ru.home.customvk.posts_screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_news.*
import ru.home.customvk.Post
import ru.home.customvk.PostUtils
import ru.home.customvk.R

class PostsActivity : AppCompatActivity(), PostsFragmentCallback {

    private companion object {
        private const val CURRENT_SCREEN_TYPE = "screen_type"
        private var currentScreenItemId: Int = ScreenType.NEWS.screenItemId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        if (savedInstanceState == null) {
            loadFragment(PostsFragment.newInstance())
        } else {
            currentScreenItemId = savedInstanceState.getInt(CURRENT_SCREEN_TYPE)
        }
        bottom_navigation.setOnNavigationItemSelectedListener {
            switchScreenIfNeeded(it.itemId)
            true
        }
    }

    private fun switchScreenIfNeeded(screenItemId: Int) {
        if (currentScreenItemId != screenItemId) {
            when (screenItemId) {
                ScreenType.NEWS.screenItemId -> {
                    currentScreenItemId = ScreenType.NEWS.screenItemId
                    loadFragment(PostsFragment.newInstance())
                }
                ScreenType.FAVORITES.screenItemId -> {
                    currentScreenItemId = ScreenType.FAVORITES.screenItemId
                    loadFragment(PostsFragment.newInstance(isFavorite = true))
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

    override fun onLikeAction(post: Post, isActionInFavoritesFragment: Boolean) {
        bottom_navigation.menu.findItem(R.id.action_favorites).isVisible = PostUtils.isLikedPostsPresent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_SCREEN_TYPE, currentScreenItemId)
    }

    enum class ScreenType(val screenItemId: Int) {
        NEWS(R.id.action_news), FAVORITES(R.id.action_favorites)
    }
}

interface PostsActivityCallback {
    fun likePostInFeed(post: Post)
}

interface PostsFragmentCallback {
    fun onLikeAction(post: Post, isActionInFavoritesFragment: Boolean)
}