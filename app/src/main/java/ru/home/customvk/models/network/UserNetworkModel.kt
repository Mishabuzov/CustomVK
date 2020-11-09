package ru.home.customvk.models.network

import com.google.gson.annotations.SerializedName

class UserNetworkModel(
    val id: Long,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("photo_50")
    val iconUrl: String?
)