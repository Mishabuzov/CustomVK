package ru.home.customvk.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import ru.home.customvk.R
import ru.home.customvk.api.ApiFactory
import ru.home.customvk.screens.posts_screen.PostsActivity
import ru.home.customvk.utils.PreferenceUtils

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = PreferenceUtils.getToken(this)
        if (accessToken.isNullOrEmpty()) {
            VK.login(this, arrayListOf(VKScope.WALL, VKScope.FRIENDS))
        } else {
            onSuccessfulLogin(accessToken)
        }
    }

    private fun Intent.addClearingStackFlags() = addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    fun onSuccessfulLogin(accessToken: String) {
        ApiFactory.accessToken = accessToken
        startActivity(
            PostsActivity.createIntent(this@LoginActivity).addClearingStackFlags()
        )
    }

    private fun restartActivity() = startActivity(
        Intent(this, LoginActivity::class.java).addClearingStackFlags()
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                PreferenceUtils.saveToken(this@LoginActivity, token.accessToken)
                onSuccessfulLogin(token.accessToken)
            }

            override fun onLoginFailed(errorCode: Int) = showNotificationDialog()
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showNotificationDialog() = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        .setTitle(R.string.authorization_failing_dialog_title)
        .setMessage(R.string.authorization_failing_dialog_message)
        .setPositiveButton(
            getString(android.R.string.ok)
        ) { dialog, _ ->
            dialog.cancel()
            restartActivity()
        }
        .setOnDismissListener { restartActivity() }
        .create()
        .show()
}