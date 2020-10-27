package ru.home.customvk.models.network

class Attachment(
    val type: String,
    val photo: Photo
)

class Photo(val sizes: List<PhotoSize>)

class PhotoSize(val url: String)