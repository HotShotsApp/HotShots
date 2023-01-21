package tw.app.hotshots.fragment.link_manager.model

import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

data class Link(
    var id: String = "",
    var url: String = "",
    var title: String = "",
    var isPinned: Boolean = false,
    var createdAt: Long = 0,
    var lastModifiedAt: Long = 0
) {
    constructor(): this(
        "",
        "",
        "",
        false,
        0,
        0
    )
}
