package tw.app.hotshots.logger

import android.app.Application
import android.util.Log
import tw.app.hotshots.BuildConfig

class Logger(
    private var className: String
) {
    private fun formatFirstPart(tag: String): String {
        return if (tag.isBlank()) {
            "$className: "
        } else {
            "$tag - $className: "
        }
    }

    private fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    public fun LogD(message: String) {
        if (isDebug())
            Log.d("$className: ", "Logger: $message")
    }

    public fun LogE(message: String) {
        if (isDebug())
            Log.e("$className: ", "Logger: $message")
    }

    public fun LogW(message: String) {
        if (isDebug())
            Log.w("$className: ", "Logger: $message")
    }

    public fun LogI(message: String) {
        if (isDebug())
            Log.i("$className: ", "Logger: $message")
    }

    public fun LogD(tag: String = "", message: String) {
        if (isDebug())
            Log.d(formatFirstPart(tag), "Logger: $message")
    }

    public fun LogE(tag: String = "", message: String) {
        if (isDebug())
            Log.e(formatFirstPart(tag), "Logger: $message")
    }

    public fun LogW(tag: String = "", message: String) {
        if (isDebug())
            Log.w(formatFirstPart(tag), "Logger: $message")
    }

    public fun LogI(tag: String = "", message: String) {
        if (isDebug())
            Log.i(formatFirstPart(tag), "Logger: $message")
    }
}