package ru.home.customvk.di.posts

import dagger.Module
import dagger.Provides
import ru.home.customvk.data.DefaultPostRepository
import ru.home.customvk.data.api.ApiFactory
import ru.home.customvk.data.database.VkDatabase
import ru.home.customvk.domain.PostRepository

@Module
class PostsViewModelModule {

    @Provides
    fun providePostRepository(apiFactory: ApiFactory, vkDatabase: VkDatabase): PostRepository {
        return DefaultPostRepository(postDao = vkDatabase.postDao(), postApi = apiFactory.postApi)
    }
}
