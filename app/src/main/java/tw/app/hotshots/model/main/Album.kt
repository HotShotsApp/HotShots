package tw.app.hotshots.model.main

import com.google.firebase.firestore.Exclude
import tw.app.hotshots.model.media.Image

data class Album(
    var uid: String = "",
    var name: String = "",
    var description: String = "",
    var createdBy: String = "",
    var createdAt: Long = 0,
    var isHidden: Boolean = false,
    var coverImage: String = "default",

    @get:Exclude
    @set:Exclude
    var images: MutableList<Image> = arrayListOf(),

    @get:Exclude
    @set:Exclude
    var likes: MutableList<Like> = arrayListOf()
) {
}