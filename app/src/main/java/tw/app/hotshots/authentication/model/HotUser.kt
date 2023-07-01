package tw.app.hotshots.authentication.model

import com.google.firebase.firestore.Exclude

data class HotUser(
    var uid: String,
    var name: String,
    var email: String,
    var description: String = "Jeszcze nie ustaliłem opisu...",
    var avatar: String = "default",

    var createdAt: Long = 0,

    var isBanned: Boolean = false,
    var banReason: String = "",
    var bannedBy: String = "",
    var bannedAt: Long = 0,
    var bannedTo: Long = 0,

    var isVerified: Boolean = false,

    var isUser: Boolean = true,
    var isModerator: Boolean = false,
    var isAdministrator: Boolean = false,

    var isPremium: Boolean = false,
    var premiumGivenAt: Long = 0,
    var premiumExpireAt: Long = 0,

    var isPinEnabled: Boolean = false,
    var pin: String = "",

    @get:Exclude
    @set:Exclude
    var userPosts: MutableList<String> = arrayListOf(),

    @get:Exclude
    @set:Exclude
    var followers: MutableList<String> = arrayListOf(),

    @get:Exclude
    @set:Exclude
    var follows: MutableList<String> = arrayListOf()
) {
    constructor(): this(
        uid = "",
        name = "",
        email = "",
        description = "Jeszcze nie ustaliłem opisu...",
        avatar = "default",
        isBanned = false,
        banReason = "",
        bannedAt = 0,
        bannedTo = 0,
        isVerified = false,
        isUser = true,
        isModerator = false,
        isAdministrator = false,
        isPremium = false,
        premiumGivenAt = 0,
        premiumExpireAt = 0
    )
}
