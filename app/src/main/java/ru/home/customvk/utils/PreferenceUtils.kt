package ru.home.customvk.utils

import android.content.Context.MODE_PRIVATE
import ru.home.customvk.VkApplication

object PreferenceUtils {

    private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES"
    private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"
    private const val POSTS_RECYCLER_KEY = "POSTS_RECYCLER"
    private const val FAVORITES_RECYCLER_KEY = "FAVORITES_RECYCLER"

    private fun getDefaultSharedPreferences() = VkApplication.instance.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    fun saveToken(accessToken: String) {
        getDefaultSharedPreferences()
            .edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .apply()
    }

    fun getToken() = getDefaultSharedPreferences().getString(ACCESS_TOKEN_KEY, "")

    fun removeToken() {
        getDefaultSharedPreferences()
            .edit()
            .remove(ACCESS_TOKEN_KEY)
            .apply()
    }

    private fun getKeyForSavingAdapterPosition(isFavoritesFragment: Boolean): String {
        return if (isFavoritesFragment) {
            FAVORITES_RECYCLER_KEY
        } else {
            POSTS_RECYCLER_KEY
        }
    }

    fun saveRecyclerPosition(position: Int, isFavoritesFragment: Boolean) {
        getDefaultSharedPreferences()
            .edit()
            .putInt(getKeyForSavingAdapterPosition(isFavoritesFragment), position)
            .apply()
    }

    fun getRecyclerPosition(isFavoritesFragment: Boolean = false): Int {
        return getDefaultSharedPreferences().getInt(getKeyForSavingAdapterPosition(isFavoritesFragment), 0)
    }
}
