package tw.app.hotshots.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor: SharedPreferences.Editor = preferences.edit()

    // KEYS
    private val IS_LINK_TITLE_AUTO_COMPLETE_ENABLED = "isLinkTitleAutoCompleteEnabled"
    private val IS_PRIVATE_VIEW_ENABLED = "isPrivateViewEnabled"
    private val IS_WARNING_ABOUT_IN_APP_BROWSER_READED = "isWarningAboutInAppBrowserReaded"
    private val IS_USER_FIRST_RUN = "isUserFirstRun"
    private val IS_BIOMETRIC_ENABLED = "isBiometricEnabled"
    private val IS_PRIVATE_LINK_VIEW_ENABLED = "isPrivateLinkViewEnabled"
    private val DEFAULT_WEB_PAGE = "defaultWebPage"

    // DEFAULT VALUES
    private val DEFAULT_WEB_PAGE_DEFVALUE = "https://www.google.com/"

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

    fun isBiometricEnabled(): Boolean {
        return getBoolean(IS_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(value: Boolean) {
        setBoolean(IS_BIOMETRIC_ENABLED, value)
    }

    fun isPrivateLinkViewEnabled(): Boolean {
        return getBoolean(IS_PRIVATE_LINK_VIEW_ENABLED, false)
    }

    fun setPrivateLinkViewEnabled(value: Boolean) {
        setBoolean(IS_PRIVATE_LINK_VIEW_ENABLED, value)
    }

    fun getDefaultWebSite(): String {
        return getString(DEFAULT_WEB_PAGE, DEFAULT_WEB_PAGE_DEFVALUE)
    }

    fun setDefaultWebSite(url: String) {
        setString(DEFAULT_WEB_PAGE, url)
    }

    /**
     * Warning
     *
     * This will delete a [SharedPreferences] data (Reset to default)
     */
    fun resetSettings() {
        editor.clear().apply()
    }

    private fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    private fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    private fun getString(key: String, defValue: String): String {
        return preferences.getString(key, defValue) ?: defValue
    }

    private fun setString(key: String, value: String) {
        editor.putString(key, value).apply()
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