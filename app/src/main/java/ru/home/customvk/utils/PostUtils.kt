package ru.home.customvk.utils

import android.webkit.MimeTypeMap
import ru.home.customvk.models.local.Post
import ru.home.customvk.models.local.PostSource
import ru.home.customvk.models.network.Attachment
import ru.home.customvk.models.network.NewsfeedObject
import ru.home.customvk.models.network.PostNetworkModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object PostUtils {
    private const val CACHED_IMAGES_PATH_FOR_SHARING = "shared_images"
    const val POSTS_IMAGE_PROVIDER_AUTHORITIES = "ru.home.customvk.imageprovider"

    private const val POSTS_TIME_PATTERN_FORMAT = "dd.MM.yyyy в HH:mm"

    private const val PHOTO_ATTACHMENT_TYPE = "photo"

    private const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"

    private const val MILLIS_IN_3_HOURS: Long = 3600 * 1000 * 3

    fun List<Post>.filterByFavorites() = filter { it.isLiked }

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
    private fun Long.convertMillisTimestampToHumanReadableDate(timeFormat: String = POSTS_TIME_PATTERN_FORMAT): String {
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

    /**
     * considered that all attachments have photo type.
     */
    private fun List<Attachment>.takeFirstPhotoUrl() =
        if (isNotEmpty()) {
            get(0).photo.sizes[0].url
        } else {
            ""
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

    fun generateFullImageName(imageUrl: String): String {
        val imageExtension = MimeTypeMap.getFileExtensionFromUrl(imageUrl)
        return "image_${System.currentTimeMillis().convertMillisTimestampToHumanReadableDate("yyyy-MM-dd_HH_mm_ss")}.$imageExtension"
    }

    fun getImageMimeTypeByUrl(url: String): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url)) ?: DEFAULT_IMAGE_MIME_TYPE

    fun isPostFresh(post: Post): Boolean = (System.currentTimeMillis() - post.insertionTimeMillis) <= MILLIS_IN_3_HOURS
}