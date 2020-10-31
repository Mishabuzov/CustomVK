package ru.home.customvk.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.home.customvk.models.network.LikeResponse
import ru.home.customvk.models.network.NewsfeedResponse

interface PostApi {

    @GET("newsfeed.get")
    fun fetchNewsfeedWithPosts(
        @Query("filters") filters: String = "post",
        @Query("count") count: Int = 10
    ): Single<NewsfeedResponse>

    @POST("likes.add")
    fun likePost(
        @Query("item_id") itemId: Long,
        @Query("owner_id") ownerId: Long,
        @Query("type") type: String = "post"
    ): Single<LikeResponse>

    @POST("likes.delete")
    fun dislikePost(
        @Query("item_id") itemId: Long,
        @Query("owner_id") ownerId: Long,
        @Query("type") type: String = "post"
    ): Single<LikeResponse>

}