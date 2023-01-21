package tw.app.hotshots.authentication.model

import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var description: String = "Jeszcze nie ustaliłem opisu...",
    var avatar: String = "default",
    var accountType: UserType = UserType.USER,
    var createdAt: Long = 0,

    var banReason: String = "",
    var bannedTo: Long = 0,
    var isBanned: Boolean = false,

    var isPinEnabled: Boolean = false,
    var pin: String = ""
) {
    constructor(): this(
        uid = UidGenerator.Generate(),
        name = "",
        email = "",
        description = "Jeszcze nie ustaliłem opisu...",
        avatar = "default",
        accountType = UserType.USER,
        createdAt = TimeUtil.currentTimeToLong(),
        banReason = "",
        bannedTo = 0,
        isBanned = false,
        isPinEnabled = false,
        pin = ""
    )
}

enum class UserType {
    BANNED,
    USER,
    PREMIUM,
    MODERATOR,
    ADMINISTRATOR
}