package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class GroupNetworkModel(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("photo_50")
    val iconUrl: String
)
