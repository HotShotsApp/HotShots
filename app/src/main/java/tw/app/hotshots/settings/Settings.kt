package tw.app.hotshots.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor: SharedPreferences.Editor = preferences.edit()

    private val IS_LINK_TITLE_AUTO_COMPLETE_ENABLED = "isLinkTitleAutoCompleteEnabled"
    private val IS_PRIVATE_VIEW_ENABLED = "isPrivateViewEnabled"
    private val IS_WARNING_ABOUT_IN_APP_BROWSER_READED = "isWarningAboutInAppBrowserReaded"
    private val IS_USER_FIRST_RUN = "isUserFirstRun"

    fun isLinkTitleAutoCompleteEnabled(): Boolean {
        return getBoolean(IS_LINK_TITLE_AUTO_COMPLETE_ENABLED, true)
    }

    fun isPrivateViewEnabled(): Boolean {
        return getBoolean(IS_PRIVATE_VIEW_ENABLED, false)
    }

    fun isWarningAboutInAppBrowserReaded(): Boolean {
        val value = getBoolean(IS_WARNING_ABOUT_IN_APP_BROWSER_READED, false)
        if (!value) setBoolean(IS_WARNING_ABOUT_IN_APP_BROWSER_READED, true)

        return value
    }

    fun isUserFirstRun(): Boolean {
        val value = getBoolean(IS_USER_FIRST_RUN, true)
        if (!value) setBoolean(IS_USER_FIRST_RUN, false)

        return getBoolean(IS_USER_FIRST_RUN, true)
    }

    private fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    private fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    companion object {
        private var settings: Settings? = null

        val getInstance: Settings
            get() {
                return settings!!
            }

        fun initialize(context: Context) {
            settings = Settings(context)
        }
    }
}