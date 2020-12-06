package ru.home.customvk.utils

import android.graphics.Bitmap
import android.webkit.MimeTypeMap
import ru.home.customvk.data.api.network_entities.Attachment
import ru.home.customvk.utils.PostUtils.convertMillisTimestampToHumanReadableDate
import java.io.OutputStream

object AttachmentUtils {

    private const val TIME_FORMAT_FOR_IMAGE_NAME = "yyyy-MM-dd_HH_mm_ss"
    private const val POSTS_MIN_IMAGE_SIZE = 250

    internal fun generateFullImageName(imageUrl: String): String {
        val imageExtension = MimeTypeMap.getFileExtensionFromUrl(imageUrl)
        return "image_${System.currentTimeMillis().convertMillisTimestampToHumanReadableDate(TIME_FORMAT_FOR_IMAGE_NAME)}.$imageExtension"
    }

    internal fun getImageMimeTypeByUrl(url: String): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url)) ?: PostUtils.DEFAULT_IMAGE_MIME_TYPE

    /**
     * Right now considered that all attachments have photo type. The method takes only first photo with not bad quality (if such is found,
     * either take the first one)
     */
    internal fun List<Attachment>.takeFirstPhotoUrl(): String {
        return if (isNotEmpty()) {
            val firstPhotoVariants = get(0).photo.sizes
            firstPhotoVariants.find { it.height > POSTS_MIN_IMAGE_SIZE && it.width > POSTS_MIN_IMAGE_SIZE }?.url
                ?: firstPhotoVariants[0].url
        } else {
            ""
        }
    }

    fun OutputStream.compressBitmap(bitmap: Bitmap) = use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }

}
