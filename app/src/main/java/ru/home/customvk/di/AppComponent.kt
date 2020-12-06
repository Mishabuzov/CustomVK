package ru.home.customvk.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import ru.home.customvk.di.login.LoginActivitySubComponent
import ru.home.customvk.di.login.LoginViewModelSubComponent
import ru.home.customvk.di.posts.PostsFragmentSubComponent
import ru.home.customvk.di.posts.PostsViewModelSubComponent
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withApplication(application: Application): Builder
        fun build(): AppComponent
    }

    fun postsViewModelSubComponentBuilder(): PostsViewModelSubComponent.Builder
    fun postsFragmentSubComponentBuilder(): PostsFragmentSubComponent.Builder

    fun loginViewModelSubComponentBuilder(): LoginViewModelSubComponent.Builder
    fun loginActivitySubComponentBuilder(): LoginActivitySubComponent.Builder

}
