package ru.home.customvk.domain

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity

@Entity(
    tableName = Post.POSTS_TABLE_NAME,
    primaryKeys = [Post.COLUMN_POST_ID, PostSource.COLUMN_SOURCE_ID]
)
data class Post(
    @ColumnInfo(name = COLUMN_POST_ID)
    val postId: Long,

    @Embedded
    val source: PostSource,

    @ColumnInfo(name = COLUMN_CREATION_DATE_MILLIS)
    val creationDateMillis: Long,

    @ColumnInfo(name = COLUMN_TEXT)
    val text: String,

    @ColumnInfo(name = COLUMN_PICTURE_URL)
    val pictureUrl: String?,

    @ColumnInfo(name = COLUMN_LIKES_COUNT)
    var likesCount: Int,

    @ColumnInfo(name = COLUMN_IS_LIKED)
    var isLiked: Boolean,

    @ColumnInfo(name = COLUMN_COMMENTS_COUNT)
    val commentsCount: Int,

    @ColumnInfo(name = COLUMN_SHARES_COUNT)
    val sharesCount: Int,

    @ColumnInfo(name = COLUMN_VIEWINGS)
    val viewings: Int,
) {
    companion object {
        const val POSTS_TABLE_NAME = "post"

        // column names:
        const val COLUMN_POST_ID = "post_id"
        const val COLUMN_CREATION_DATE_MILLIS = "insertion_time_millis"
        const val COLUMN_TEXT = "text"
        const val COLUMN_PICTURE_URL = "picture_url"
        const val COLUMN_LIKES_COUNT = "likes_count"
        const val COLUMN_IS_LIKED = "is_liked"
        const val COLUMN_COMMENTS_COUNT = "comments_count"
        const val COLUMN_SHARES_COUNT = "shares_count"
        const val COLUMN_VIEWINGS = "viewings"
    }
}

// Post's source can be an user or a group.
data class PostSource(
    @ColumnInfo(name = COLUMN_SOURCE_ID)
    val sourceId: Long,

    @ColumnInfo(name = COLUMN_SOURCE_NAME)
    val sourceName: String,

    @ColumnInfo(name = COLUMN_SOURCE_ICON_URL)
    val sourceIconUrl: String
) {
    companion object {
        const val COLUMN_SOURCE_ID = "source_id"
        const val COLUMN_SOURCE_NAME = "source_name"
        const val COLUMN_SOURCE_ICON_URL = "source_icon_url"
    }
}
