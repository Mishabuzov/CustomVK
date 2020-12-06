package ru.home.customvk.di.login

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import ru.home.customvk.presentation.login_screen.LoginActivity
import ru.home.customvk.presentation.login_screen.LoginViewModel

@Module
class LoginActivityModule {

    @Provides
    fun loginViewModel(loginActivity: LoginActivity, app: Application): LoginViewModel {
        return ViewModelProvider(loginActivity, ViewModelProvider.AndroidViewModelFactory.getInstance(app)).get(LoginViewModel::class.java)
    }
}
