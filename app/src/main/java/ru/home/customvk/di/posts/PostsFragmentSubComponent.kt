package ru.home.customvk.di.posts

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import ru.home.customvk.presentation.posts_screen.PostsFragment

@Subcomponent(modules = [PostsFragmentModule::class])
interface PostsFragmentSubComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun with(fragment: Fragment): Builder

        fun build(): PostsFragmentSubComponent
    }

    fun inject(postsFragment: PostsFragment)
}
