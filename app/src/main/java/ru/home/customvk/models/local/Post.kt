package ru.home.customvk.models.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity

@Entity(
    tableName = "post",
    primaryKeys = ["post_id", "source_id"]
)
data class Post(
    @ColumnInfo(name = "post_id")
    val postId: Long,

    @Embedded
    val source: PostSource,

    @ColumnInfo(name = "insertion_time_millis")
    val insertionTimeMillis: Long,

    @ColumnInfo(name = "readable_publication_date")
    val readablePublicationDate: String,

    @ColumnInfo(name = "text")
    var text: String,

    @ColumnInfo(name = "picture_url")
    val pictureUrl: String,

    @ColumnInfo(name = "likes_count")
    var likesCount: Int,

    @ColumnInfo(name = "is_liked")
    var isLiked: Boolean,

    @ColumnInfo(name = "comments_count")
    val commentsCount: Int,

    @ColumnInfo(name = "shares_count")
    val sharesCount: Int,

    @ColumnInfo(name = "viewings")
    val viewings: Int,
)

// Post's source can be an user or a group.
data class PostSource(
    @ColumnInfo(name = "source_id")
    val sourceId: Long,

    @ColumnInfo(name = "source_name")
    val sourceName: String,

    @ColumnInfo(name = "source_icon_url")
    val sourceIconUrl: String
)