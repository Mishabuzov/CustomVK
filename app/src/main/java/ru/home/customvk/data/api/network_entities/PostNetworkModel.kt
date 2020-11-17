package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class PostNetworkModel(
    // if > 0 -> user's post
    // if < 0 -> group's post
    @SerializedName("source_id")
    val sourceId: Long,

    val date: Long,

    @SerializedName("post_id")
    val postId: Long,

    val text: String,

    val attachments: List<Attachment>?,

    val comments: CommentsNetworkModel,

    val likes: LikesNetworkModel,

    val reposts: RepostsNetworkModel,

    val views: ViewingsNetworkModel?
)

class CommentsNetworkModel(val count: Int)

class LikesNetworkModel(
    val count: Int,

    // 1 - positive, 0 - negative
    @SerializedName("user_likes")
    val isLiked: Int,
)

class RepostsNetworkModel(val count: Int)

class ViewingsNetworkModel(val count: Int)
