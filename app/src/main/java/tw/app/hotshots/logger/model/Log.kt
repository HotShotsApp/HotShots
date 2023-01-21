package tw.app.hotshots.logger.model

import tw.app.hotshots.logger.LogType

data class Log(
    var message: String = "",
    var createdAt: Long = 0,
    var type: LogType = LogType.NORMAL
) {
}