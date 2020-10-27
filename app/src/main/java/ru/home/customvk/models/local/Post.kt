package ru.home.customvk.models.local

data class Post(
    val id: Long,
    val source: PostSource,
    val publicationDate: String,
    var text: String,
    val pictureUrl: String,
    var likesCount: Int,
    var isLiked: Boolean,
    val commentsCount: Int,
    val sharesCount: Int,
    val viewings: String,
)

// Post's source can be an user or a group.
data class PostSource(
    val id: Long,
    val name: String,
    val iconUrl: String
)