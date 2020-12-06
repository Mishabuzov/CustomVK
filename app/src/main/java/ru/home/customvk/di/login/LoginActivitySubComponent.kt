package ru.home.customvk.di.login

import dagger.BindsInstance
import dagger.Subcomponent
import ru.home.customvk.presentation.login_screen.LoginActivity

@Subcomponent(modules = [LoginActivityModule::class])
interface LoginActivitySubComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun with(loginActivity: LoginActivity): Builder

        fun build(): LoginActivitySubComponent
    }

    fun inject(loginActivity: LoginActivity)
}