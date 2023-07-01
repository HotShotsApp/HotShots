package tw.app.hotshots.app.database.models

data class Version(
    var version: String,
    var state: STATE,
    var required: Boolean,
    var downloadUrl: String,
    var changelog: String
) {
    constructor(): this(
        version = "",
        state = STATE.ALPHA,
        required = false,
        downloadUrl = "",
        changelog = ""
    )
}

public enum class STATE {
    ALPHA,
    BETA,
    STABLE
}