package ru.home.customvk.data.api.network_entities

class Attachment(
    val type: String,
    val photo: Photo
)

class Photo(val sizes: List<PhotoSize>)

class PhotoSize(val url: String)