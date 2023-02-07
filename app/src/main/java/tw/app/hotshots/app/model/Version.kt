package tw.app.hotshots.app.model

data class Version(
    var changeLog: String = "",
    var downloadUrl: String = "",
    var required: Boolean = false,
    var versionCode: Int = 1,
    var versionName: String = ""
) {
    constructor(): this(
        changeLog = "",
        downloadUrl = "",
        required = false,
        versionCode = 1,
        versionName = ""
    )
}