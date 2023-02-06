package tw.app.hotshots.model.main

import com.google.firebase.firestore.Exclude
import tw.app.hotshots.util.TimeUtil

data class Influencer(
    var uid: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var nickname: String = "",
    var avatar: String = "default",
    var description: String = "Opis nie został jeszcze ustalony...",
    var moderatorUid: String = "",
    var createdBy: String = "",
    var addedBy: String = "",
    var albumsCount: Int = 0,
    var createdAt: Long = TimeUtil.currentTimeToLong(),
    var isPremium: Boolean = false,
    var isVerified: Boolean = false,
    var isHidden: Boolean = false,

    @get:Exclude
    @set:Exclude
    var follows: MutableList<Follow> = arrayListOf()
) {
    constructor(): this(
        uid = "",
        firstName = "",
        lastName = "",
        nickname = "",
        avatar = "",
        description = "Opis nie został jeszcze ustalony...",
        moderatorUid = "",
        albumsCount = 0,
        createdBy = "",
        addedBy = "",
        createdAt = TimeUtil.currentTimeToLong()
    )
}