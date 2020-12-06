package ru.home.customvk.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_login.*
import ru.home.customvk.R
import ru.home.customvk.VkApplication
import ru.home.customvk.presentation.posts_screen.PostsActivity
import ru.home.customvk.utils.PreferencesUtils
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesUtils: PreferencesUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        (applicationContext as VkApplication).appComponent.inject(this)

        val accessToken = preferencesUtils.getToken()
        if (accessToken.isNullOrEmpty()) {  // VKScope.OFFLINE grants endless token, just check if it was already got
            startLogin()
        } else {
            onSuccessfulLogin(accessToken)
        }

        loginButton.setOnClickListener {
            startLogin()
            loginButton.isEnabled = false
        }
    }

    private fun startLogin() = VK.login(this, arrayListOf(VKScope.WALL, VKScope.FRIENDS, VKScope.OFFLINE))

    private fun onSuccessfulLogin(accessToken: String) {
        preferencesUtils.accessToken = accessToken
        startActivity(PostsActivity.createIntent(this).addClearingStackFlags())
    }

    private fun Intent.addClearingStackFlags() = addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                preferencesUtils.saveToken(token.accessToken)
                onSuccessfulLogin(token.accessToken)
            }

            /**
             * Is called when clicked "cancel" in the WebView auth screen.
             */
            override fun onLoginFailed(errorCode: Int) = activateLoginButton()
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {  // true when back button is pressed.
            super.onActivityResult(requestCode, resultCode, data)
            activateLoginButton()
        }
    }

    private fun activateLoginButton() {
        loginButton.isVisible = true
        loginButton.isEnabled = true
    }
}
