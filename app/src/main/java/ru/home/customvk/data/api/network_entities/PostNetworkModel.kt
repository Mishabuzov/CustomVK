package ru.home.customvk.data.api.network_entities

import com.google.gson.annotations.SerializedName

class PostNetworkModel(
    // if > 0 -> user's post
    // if < 0 -> group's post
    @SerializedName("source_id")
    val sourceId: Long,

    @SerializedName("date")
    val date: Long,

    @SerializedName("post_id")
    val postId: Long,

    @SerializedName("text")
    val text: String,

    @SerializedName("attachments")
    val attachments: List<Attachment>?,

    @SerializedName("comments")
    val comments: CommentsNetworkModel,

    @SerializedName("likes")
    val likes: LikesNetworkModel,

    @SerializedName("reposts")
    val reposts: RepostsNetworkModel,

    @SerializedName("views")
    val views: ViewingsNetworkModel?
)

class CommentsNetworkModel(
    @SerializedName("count")
    val count: Int
)

class LikesNetworkModel(
    @SerializedName("count")
    val count: Int,

    // 1 - positive, 0 - negative
    @SerializedName("user_likes")
    val isLiked: Int,
)

class RepostsNetworkModel(
    @SerializedName("count")
    val count: Int
)

class ViewingsNetworkModel(
    @SerializedName("count")
    val count: Int
)
