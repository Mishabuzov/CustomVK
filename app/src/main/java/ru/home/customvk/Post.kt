package ru.home.customvk

class Post(
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (groupName != other.groupName) return false
        if (groupLogo != other.groupLogo) return false
        if (date != other.date) return false
        if (textContent != other.textContent) return false
        if (pictureName != other.pictureName) return false
        if (isFavorite != other.isFavorite) return false
        if (likesCount != other.likesCount) return false
        if (commentsCount != other.commentsCount) return false
        if (sharesCount != other.sharesCount) return false
        if (viewings != other.viewings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupLogo.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + textContent.hashCode()
        result = 31 * result + pictureName.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + likesCount
        result = 31 * result + commentsCount
        result = 31 * result + sharesCount
        result = 31 * result + viewings.hashCode()
        return result
    }
}