package tw.app.hotshots.settings

import android.os.Bundle
import tw.app.hotshots.R
import tw.app.hotshots.activity.BaseActivity
import tw.app.hotshots.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {

    private var _binding: ActivitySettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = getString(R.string.settings)

        _binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.settings_screen, SettingsFragment())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}