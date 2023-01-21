package tw.app.hotshots.model.main

import com.google.firebase.firestore.Exclude
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.model.media.Image
import tw.app.hotshots.util.TimeUtil

data class Post(
    var uid: String = "",
    var userUid: String = "",
    var description: String = "",
    var influencerUid: String = "none",

    @get:Exclude
    @set:Exclude
    var images: MutableList<Image>,

    @get:Exclude
    @set:Exclude
    var likes: MutableList<Like>,
    var albumUid: String = "none",
    var isHidden: Boolean = false,
    var isCommentsAllowed: Boolean = true,
    var createdAt: Long = TimeUtil.currentTimeToLong(),

    @get:Exclude
    @set:Exclude
    var user: User = User(),

    @get:Exclude
    @set:Exclude
    var userLikedPost: Boolean = false
) {
    constructor() : this(
        uid = "",
        userUid = "",
        description = "",
        influencerUid = "none",
        images = arrayListOf(),
        likes = arrayListOf(),
        albumUid = "none",
        isHidden = false,
        isCommentsAllowed = true,
        createdAt = TimeUtil.currentTimeToLong(),
        user = User(),
        userLikedPost = false
    )
}
