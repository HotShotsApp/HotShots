package tw.app.hotshots.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings(
    val preferences: SharedPreferences
) {
    private val editor = preferences.edit()

    private val STANDARD_POST_DISPLAY = "standardPostDisplay"
    private val HIDE_IMAGES_IN_MAIN_PAGE = "hideImagesInMainPage"
    private val STRONG_COMPRESS_IMAGES_IN_MAIN_PAGE = "strongCompressImagesInMainPage"
    private val IS_AUTOPLAY_ENABLED = "isAutoplayEnabled"
    private val SLOW_MOTION_SPEED = "slowMotionSpeed"

    public fun isStandardPostDisplayEnabled(): Boolean {
        return getBoolean(STANDARD_POST_DISPLAY)
    }

    public fun isHideImagesInMainPageEnabled(): Boolean {
        return getBoolean(HIDE_IMAGES_IN_MAIN_PAGE)
    }

    public fun isStrongCompressImagesInMainPageEnabled(): Boolean {
        return getBoolean(STRONG_COMPRESS_IMAGES_IN_MAIN_PAGE)
    }

    public fun isAutoPlayEnabled(): Boolean {
        return getBoolean(IS_AUTOPLAY_ENABLED)
    }

    public fun getSlowMotionSpeed(): Float {
        return getFloat(SLOW_MOTION_SPEED) / 10
    }

    public fun setSlowMotionSpeed(speed: Float) {
        setFloat(SLOW_MOTION_SPEED, speed)
    }

    private fun setString(key: String, value: String = "") {
        editor.putString(key, value).commit()
    }

    private fun getString(key: String, defValue: String = ""): String {
        return preferences.getString(key, defValue) ?: defValue
    }

    private fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).commit()
    }

    private fun getBoolean(key: String, defValue: Boolean = false): Boolean {
        return preferences.getBoolean(key, defValue) ?: defValue
    }

    private fun setFloat(key: String, defValue: Float = 0.0f) {
        editor.putFloat(key, defValue).commit()
    }

    private fun getFloat(key: String, value: Float = 0.0f): Float {
        return preferences.getFloat(key, value)
    }
}