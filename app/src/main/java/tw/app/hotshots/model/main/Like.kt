package tw.app.hotshots.model.main

data class Like(
    var likedBy: String = ""
){
    constructor(): this(
        likedBy = ""
    )
}
