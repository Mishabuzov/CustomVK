package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class GroupNetworkModel(
    val id: Long,

    val name: String,

    @SerializedName("photo_50")
    val iconUrl: String
)