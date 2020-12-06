package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class Attachment(
    @SerializedName("type")
    val type: String,

    @SerializedName("photo")
    val photo: Photo
)

class Photo(
    @SerializedName("sizes")
    val sizes: List<PhotoSize>
)

class PhotoSize(
    @SerializedName("url")
    val url: String,

    @SerializedName("height")
    val height: Int,

    @SerializedName("width")
    val width: Int
)
