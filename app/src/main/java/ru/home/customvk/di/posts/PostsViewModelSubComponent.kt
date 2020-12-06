package ru.home.customvk.di.posts

import dagger.Subcomponent
import ru.home.customvk.presentation.posts_screen.PostsViewModel

@Subcomponent(modules = [PostsViewModelModule::class])
interface PostsViewModelSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): PostsViewModelSubComponent
    }

    fun inject(postsViewModel: PostsViewModel)
}
