package ru.home.customvk.models.network

import com.google.gson.annotations.SerializedName

class NewsfeedResponse(@SerializedName("response") val newsfeed: NewsfeedObject)

class NewsfeedObject(
    @SerializedName("items")
    val posts: List<PostNetworkModel>,

    @SerializedName("profiles")
    val users: List<User>,

    val groups: List<Group>
)