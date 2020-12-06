package ru.home.customvk.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import javax.inject.Inject

class PreferencesUtils @Inject constructor(private val appContext: Context) {

    private companion object {
        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES"
        private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"
        private const val POSTS_RECYCLER_KEY = "POSTS_RECYCLER"
        private const val FAVORITES_RECYCLER_KEY = "FAVORITES_RECYCLER"
    }

    lateinit var accessToken: String

    private fun getDefaultSharedPreferences() = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    fun saveToken(accessToken: String) {
        this.accessToken = accessToken
        getDefaultSharedPreferences()
            .edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .apply()
    }

    fun getToken() = getDefaultSharedPreferences().getString(ACCESS_TOKEN_KEY, "")

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
