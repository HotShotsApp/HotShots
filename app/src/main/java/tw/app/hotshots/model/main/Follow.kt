package tw.app.hotshots.model.main

data class Follow(
    var followedBy: String = ""
){
    constructor(): this(
        followedBy = ""
    )
}