package tw.app.hotshots.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tw.app.hotshots.R
import tw.app.hotshots.activity.LicenseActivity

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var licenseButton: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_preferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
        init()
    }

    private fun setup() {
        licenseButton = findPreference("licenseButton")!!
    }

    private fun init() {
        licenseButton.setOnPreferenceClickListener {
            var intent: Intent? = Intent(requireActivity(), LicenseActivity::class.java)
            startActivity(intent)
            intent = null
            false
        }
    }
}