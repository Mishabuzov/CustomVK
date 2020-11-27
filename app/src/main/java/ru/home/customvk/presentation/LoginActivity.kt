package ru.home.customvk.presentation

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import ru.home.customvk.R
import ru.home.customvk.data.api.ApiFactory
import ru.home.customvk.presentation.posts_screen.PostsActivity
import ru.home.customvk.utils.PreferenceUtils

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = PreferenceUtils.getToken()
        if (accessToken.isNullOrEmpty()) {
            VK.login(this, arrayListOf(VKScope.WALL, VKScope.FRIENDS, VKScope.OFFLINE))
        } else {
            onSuccessfulLogin(accessToken)
        }
        VK.addTokenExpiredHandler(tokenTracker)
    }

    private val tokenTracker = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            restartActivity()
            PreferenceUtils.removeToken()
            showNotificationDialog(R.string.token_expired_message)
        }
    }

    private fun Intent.addClearingStackFlags() = addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    fun onSuccessfulLogin(accessToken: String) {
        ApiFactory.accessToken = accessToken
        startActivity(PostsActivity.createIntent(this@LoginActivity).addClearingStackFlags())
    }

    private fun restartActivity() = startActivity(Intent(this, LoginActivity::class.java).addClearingStackFlags())

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                PreferenceUtils.saveToken(token.accessToken)
                onSuccessfulLogin(token.accessToken)
            }

            override fun onLoginFailed(errorCode: Int) = showNotificationDialog(R.string.authorization_failing_dialog_message)
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showNotificationDialog(@StringRes messageRes: Int) {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
            .setTitle(R.string.authorization_failing_dialog_title)
            .setMessage(messageRes)
            .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                dialog.cancel()
                restartActivity()
            }
            .setOnDismissListener { restartActivity() }
            .create()
            .show()
    }
}