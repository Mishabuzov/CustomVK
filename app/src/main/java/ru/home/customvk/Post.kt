package ru.home.customvk

data class Post(
    val id: Int,
    val groupName: String,
    val groupLogo: String,
    val date: String,
    var textContent: String,
    val pictureName: String,
    var isFavorite: Boolean,
    var likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val viewings: String,
)