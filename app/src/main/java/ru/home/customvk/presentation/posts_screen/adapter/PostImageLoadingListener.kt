package ru.home.customvk.presentation.posts_screen.adapter

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class PostImageLoadingListener(private val setupShareImageListener: (Bitmap) -> Unit) : RequestListener<Bitmap> {

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
        Log.e("M_GifRequestListener", "Error Loading image of post\n", e)
        return false
    }

    override fun onResourceReady(
        resource: Bitmap,
        model: Any?,
        target: Target<Bitmap>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        setupShareImageListener(resource)
        return false
    }
}