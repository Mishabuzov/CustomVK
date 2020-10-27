package ru.home.customvk.models.network

import com.google.gson.annotations.SerializedName

class Group(
    val id: Long,

    val name: String,

    @SerializedName("photo_50")
    val iconUrl: String
)