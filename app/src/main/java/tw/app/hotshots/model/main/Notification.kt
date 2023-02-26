package tw.app.hotshots.model.main

import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

data class Notification(
    var uid: String = UidGenerator.Generate(18),
    var title: String = "",
    var message: String = "",
    var createdAt: Long = TimeUtil.currentTimeToLong(),
    var isMarkedAsRead: Boolean = false,

    var containsExtras: Boolean = false,
    var extraType: ExtraType = ExtraType.NONE,
    var extraValue: String = ""
) {
}

enum class ExtraType {
    NONE,
    OPEN_INFLUENCER,
    OPEN_ALBUM,
    OPEN_POST,
    OPEN_DIALOG
}