package tw.app.hotshots.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor: SharedPreferences.Editor = preferences.edit()

    private val IS_LINK_TITLE_AUTO_COMPLETE_ENABLED = "isLinkTitleAutoCompleteEnabled"

    fun isLinkTitleAutoCompleteEnabled(): Boolean {
        return getBoolean(IS_LINK_TITLE_AUTO_COMPLETE_ENABLED)
    }

    private fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, true)
    }

    companion object {
        private var settings: Settings? = null

        val getInstance: Settings
            get() {
                return Settings.settings!!
            }

        fun initialize(context: Context) {
            settings = Settings(context)
        }
    }
}