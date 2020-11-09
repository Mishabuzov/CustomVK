package ru.home.customvk.models.network

import com.google.gson.annotations.SerializedName

class LikeResponse(@SerializedName("response") val likesInfo: LikesCountObject)

class LikesCountObject(@SerializedName("likes") val count: Int)