package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class HidingResponse(
    @SerializedName("response")
    val vkResponseCode: Int
)
