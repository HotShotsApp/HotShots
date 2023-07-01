package tw.app.hotshots.license.model

data class License(
    var name: String,
    var author: String,
    var url: String,
    var version: String,
    var shortDescription: String,
    var icon: Int,
) {
}