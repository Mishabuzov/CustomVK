package ru.home.customvk.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

object PreferenceUtils {

    private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES"
    private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"

    fun saveToken(context: Context, accessToken: String) =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .apply()

    fun getToken(context: Context) =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .getString(ACCESS_TOKEN_KEY, "")
}