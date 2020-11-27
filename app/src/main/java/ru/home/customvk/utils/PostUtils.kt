package ru.home.customvk.utils

import ru.home.customvk.data.api.network_entities.NewsfeedObject
import ru.home.customvk.data.api.network_entities.PostNetworkModel
import ru.home.customvk.domain.Post
import ru.home.customvk.domain.PostSource
import ru.home.customvk.utils.AttachmentUtils.takeFirstPhotoUrl
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object PostUtils {
    private const val CACHED_IMAGES_PATH_FOR_SHARING = "shared_images"
    const val POSTS_IMAGE_PROVIDER_AUTHORITIES = "ru.home.customvk.imageprovider"

    private const val POSTS_TIME_PATTERN_FORMAT = "dd.MM.yyyy в HH:mm"

    private const val PHOTO_ATTACHMENT_TYPE = "photo"

    internal const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"

    fun List<Post>.filterByFavorites() = filter { it.isLiked }

    fun List<Post>.areLikedPostsPresent(): Boolean = filterByFavorites().count() > 0

    fun createFileToCacheBitmap(bitmapFullName: String, dirToSave: File): File {
        val fullPathToSave = File(dirToSave, CACHED_IMAGES_PATH_FOR_SHARING)
        if (!fullPathToSave.exists()) {
            fullPathToSave.mkdirs()
        }
        return File(fullPathToSave, bitmapFullName)
    }

    /**
     * convert milliseconds since January 1, 1970 to readable date in the provided format.
     */
    internal fun Long.convertMillisTimestampToHumanReadableDate(timeFormat: String = POSTS_TIME_PATTERN_FORMAT): String {
        val dateInMillisecond = Date(this)
        val sdf = SimpleDateFormat(timeFormat, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT+3")
        return sdf.format(dateInMillisecond)
    }

    private fun PostNetworkModel.toPost(sourceName: String, sourceIconUrl: String) = Post(
        postId = postId,
        source = PostSource(sourceId, sourceName, sourceIconUrl),
        insertionTimeMillis = System.currentTimeMillis(),
        readablePublicationDate = (date * 1000).convertMillisTimestampToHumanReadableDate(),
        text = text,
        pictureUrl = attachments?.filter { it.type == PHOTO_ATTACHMENT_TYPE }?.takeFirstPhotoUrl() ?: "",
        likesCount = likes.count,
        isLiked = likes.isLiked == 1,
        commentsCount = comments.count,
        sharesCount = reposts.count,
        viewings = views?.count ?: 0
    )

    private fun Post.setLikedAndIncreaseLikesCount() {
        isLiked = true
        likesCount++
    }

    private fun Post.setDislikedAndDecreaseLikesCount() {
        isLiked = false
        likesCount--
    }

    /**
     * Likes (or dislikes) post at "postIndex" position, and returns it as the result.
     */
    fun MutableList<Post>.likePostAtPosition(postIndex: Int): Post {
        val postToUpdate = this[postIndex].copy()
        if (postToUpdate.isLiked) {
            postToUpdate.setDislikedAndDecreaseLikesCount()
        } else {
            postToUpdate.setLikedAndIncreaseLikesCount()
        }
        this[postIndex] = postToUpdate
        return postToUpdate
    }

    fun NewsfeedObject.toPosts(): List<Post> {
        val extractedPosts: MutableList<Post> = mutableListOf()
        posts.forEach { networkPost ->
            val sourceName: String
            val sourceIconUrl: String
            if (networkPost.sourceId > 0) {  // the condition means that the post was published by some user.
                val user = users.find { it.id == networkPost.sourceId }
                sourceName = "${user?.firstName} ${user?.lastName}"
                sourceIconUrl = user?.iconUrl ?: ""
            } else {  // either the post was published by some group.
                val group = groups.find { it.id == abs(networkPost.sourceId) }
                sourceName = group?.name ?: ""
                sourceIconUrl = group?.iconUrl ?: ""
            }
            extractedPosts.add(networkPost.toPost(sourceName, sourceIconUrl))
        }
        return extractedPosts
    }
}
