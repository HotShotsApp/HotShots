package tw.app.hotshots.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    val ONE_SECOND = 1000L;
    val TEN_SECONDS = 10000L;

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun convertLongToTime(time: Long, pattern: String): String {
        val date = Date(time)
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(date)
    }

    fun currentTimeToLong(): Long {
        return System.currentTimeMillis()
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        return df.parse(date)?.time ?: 0
    }
}