package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class UserNetworkModel(
    @SerializedName("id")
    val id: Long,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("photo_50")
    val iconUrl: String
)
