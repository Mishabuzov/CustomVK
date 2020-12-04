package ru.home.customvk.di.posts

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import ru.home.customvk.presentation.posts_screen.PostsViewModel

@Module
class PostsFragmentModule {

    @Provides
    fun postViewModel(fragment: Fragment, app: Application): PostsViewModel {
        return ViewModelProvider(fragment, ViewModelProvider.AndroidViewModelFactory.getInstance(app)).get(PostsViewModel::class.java)
    }
}
