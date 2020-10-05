package ru.home.customviewapplication.posts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.view_post.*
import ru.home.customviewapplication.R
import ru.home.customviewapplication.posts.PostUtils.POSTS_STUB_JSON_FILE

class PostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        getPostsAndShowFirst()
    }

    private fun getPosts(): List<Post> {
        val jsonString: String = assets.open(POSTS_STUB_JSON_FILE).bufferedReader().use { it.readText() }
        return PostUtils.parsePosts(jsonString)
    }

    private fun getPostsAndShowFirst() = showPost(getPosts()[0])

    private fun showPost(post: Post) = with(post) {
        avatarImageView.setImageResource(resources.getIdentifier(groupLogo, "drawable", packageName))
        groupNameTextView.text = groupName
        timeTextView.text = date
        mainTextView.text = textContent
        postImageView.setImageResource(resources.getIdentifier(pictureName, "drawable", packageName))
        if (likesCount > 0) {
            likeButton.text = likesCount.toString()
        }
        if (commentsCount > 0) {
            commentButton.text = commentsCount.toString()
        }
        if (sharesCount > 0) {
            shareButton.text = sharesCount.toString()
        }
        if (viewings.isNotEmpty()) {
            viewingTextView.text = viewings
        }
    }
}