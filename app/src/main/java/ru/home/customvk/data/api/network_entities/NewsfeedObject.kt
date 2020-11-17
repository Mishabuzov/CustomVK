package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class NewsfeedResponse(@SerializedName("response") val newsfeed: NewsfeedObject)

class NewsfeedObject(
    @SerializedName("items")
    val posts: List<PostNetworkModel>,

    @SerializedName("profiles")
    val users: List<UserNetworkModel>,

    val groups: List<GroupNetworkModel>
)