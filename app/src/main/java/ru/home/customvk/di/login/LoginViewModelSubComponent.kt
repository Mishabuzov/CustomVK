package ru.home.customvk.di.login

import dagger.Subcomponent
import ru.home.customvk.presentation.login_screen.LoginViewModel

@Subcomponent
interface LoginViewModelSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): LoginViewModelSubComponent
    }

    fun inject(loginViewModel: LoginViewModel)
}
