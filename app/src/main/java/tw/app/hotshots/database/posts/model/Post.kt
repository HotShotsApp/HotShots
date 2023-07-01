package tw.app.hotshots.database.posts.model

import com.google.firebase.firestore.Exclude
import tw.app.hotshots.singleton.UserSingleton
import java.util.UUID

data class Post(
    var uid: String = UUID.randomUUID().toString(),
    var createdBy: String = "",
    var description: String = "",
    var approvedBy: String = "", // The moderator UID that approved post.
    var mainImageUrl: String = "",

    var createdAt: Long = 0,
    var hiddenAt: Long = 0,
    var lastTimeEditedAt: Long = 0,

    var isImageAttached: Boolean = false,
    var isMultipleImagesAvailable: Boolean = false,
    var isHidden: Boolean = false,
    var isCommentsEnabled: Boolean = true,

    @get:Exclude
    @set:Exclude
    var images: MutableList<String> = arrayListOf(),

    @get:Exclude
    @set:Exclude
    var likes: MutableList<String> = arrayListOf(),

    @get:Exclude
    @set:Exclude
    var comments: MutableList<String> = arrayListOf()
) {
    constructor(): this(
        uid = UUID.randomUUID().toString(),
        createdBy = UserSingleton.instance?.user?.uid ?: "",
        description = "",
        approvedBy = "",
        mainImageUrl = "",
        createdAt = 0,
        hiddenAt = 0,
        lastTimeEditedAt = 0,
        isImageAttached = false,
        isMultipleImagesAvailable = false,
        isHidden = false,
        isCommentsEnabled = true,
        images = arrayListOf(),
        likes = arrayListOf(),
        comments = arrayListOf()
    )
}