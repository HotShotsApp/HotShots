package tw.app.hotshots.logger

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tw.app.hotshots.logger.model.Log
import tw.app.hotshots.util.TimeUtil

/**
 * Simple [Logger] for HotShots App
 * You can later check available logs in App Settings or [DebugActivity]
 * or by simply call and handling Logger#getLogs
 */
class Logger {
    companion object {
        private var _loggerInstance: Logger? = null
        private val loggerInstance get() = _loggerInstance!!
        private var _context: Context? = null
        private val context get() = _context!!

        private lateinit var logsList: MutableList<Log>
        private lateinit var loggerStorage: SharedPreferences

        /**
         * Parse & save log to SharedPreferences
         */
        fun log(message: String, type: LogType) {
            if (_loggerInstance == null) {
                throw Exception("Logger is not initialized!")
            } else {
                val log = Log(
                    message = message,
                    createdAt = TimeUtil.currentTimeToLong(),
                    type = type
                )

                logsList.reverse()
                logsList.add(log)
                logsList.reverse()
            }
        }

        private fun save() {
            loggerStorage.edit().putString("logs", Gson().toJson(logsList)).apply()
        }

        /**
         * Get Available App Logs
         * @return List with Available Logs
         */
        fun getLogs(): MutableList<Log> {
            return logsList
        }

        fun clearLogs() {
            logsList = arrayListOf()
            loggerStorage.edit().putString("logs", "").apply()
        }

        fun refresh(): MutableList<Log> {
            save()
            logsList.reverse()
            val itemType = object : TypeToken<MutableList<Log>>() {}.type
            val jsonString = loggerStorage.getString("logs", "")
            if (jsonString.isNullOrBlank())
                logsList = arrayListOf()
            else
                logsList = Gson().fromJson(loggerStorage.getString("logs", ""), itemType)

            return logsList
        }

        /**
         * Initialize Logger (in Application class)
         */
        fun initialize(contextNew: Context) {
            if (_loggerInstance == null) {
                _loggerInstance = Logger()
                _context = contextNew
                loggerStorage = context.getSharedPreferences("logger", MODE_PRIVATE)

                val itemType = object : TypeToken<MutableList<Log>>() {}.type
                val jsonString = loggerStorage.getString("logs", "")
                if (jsonString.isNullOrBlank())
                    logsList = arrayListOf()
                else
                    logsList = Gson().fromJson(loggerStorage.getString("logs", ""), itemType)

                logsList.reverse()
            }
        }
    }
}

enum class LogType(value: Int) {
    INITIALIZATION(0),
    NORMAL(1),
    ERROR(2),
    CRITICAL(3);

    private var logValue = 0

    init {
        logValue = value
    }

    fun getValue(): Int {
        return logValue
    }
}