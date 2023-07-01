package tw.app.hotshots.database.comments.model

import com.google.firebase.firestore.Exclude
import java.util.UUID

data class Comment(
    var uid: String = UUID.randomUUID().toString(),
    var content: String = "",
    var addedBy: String = "",

    var addedAt: Long = 0,

    @get:Exclude
    @set:Exclude
    var isResponse: Boolean = false,

    @get:Exclude
    @set:Exclude
    var responses: MutableList<Comment> = arrayListOf(),

    @get:Exclude
    @set:Exclude
    var likes: MutableList<String> = arrayListOf()
) {
}