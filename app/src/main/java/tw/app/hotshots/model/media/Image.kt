package tw.app.hotshots.model.media

data class Image(
    val uid: String,
    val imageUrl: String,
    val order: Int
) {
    constructor(): this(
        uid = "",
        imageUrl = "",
        order = 0
    )
}
