package ru.home.customvk.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import ru.home.customvk.di.posts.PostsFragmentSubComponent
import ru.home.customvk.di.posts.PostsViewModelSubComponent
import ru.home.customvk.presentation.LoginActivity
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

    fun inject(loginActivity: LoginActivity)
}
