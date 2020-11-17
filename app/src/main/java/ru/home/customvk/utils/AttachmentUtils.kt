package ru.home.customvk.utils

import android.graphics.Bitmap
import android.webkit.MimeTypeMap
import ru.home.customvk.data.api.network_entities.Attachment
import ru.home.customvk.utils.PostUtils.convertMillisTimestampToHumanReadableDate
import java.io.OutputStream

object AttachmentUtils {

    private const val TIME_FORMAT_FOR_IMAGE_NAME = "yyyy-MM-dd_HH_mm_ss"

    internal fun generateFullImageName(imageUrl: String): String {
        val imageExtension = MimeTypeMap.getFileExtensionFromUrl(imageUrl)
        return "image_${System.currentTimeMillis().convertMillisTimestampToHumanReadableDate(TIME_FORMAT_FOR_IMAGE_NAME)}.$imageExtension"
    }

    internal fun getImageMimeTypeByUrl(url: String): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url)) ?: PostUtils.DEFAULT_IMAGE_MIME_TYPE

    /**
     * considered that all attachments have photo type.
     */
    internal fun List<Attachment>.takeFirstPhotoUrl() =
        if (isNotEmpty()) {
            get(0).photo.sizes[0].url
        } else {
            ""
        }

    fun OutputStream.compressBitmap(bitmap: Bitmap) = use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }

}