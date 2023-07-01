package tw.app.hotshots.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import tw.app.hotshots.databinding.ActivityLicenseBinding
import tw.app.hotshots.license.LicenseManager
import tw.app.hotshots.license.adapter.LicenseAdapter

class LicenseActivity : AppCompatActivity() {

    private var _binding: ActivityLicenseBinding? = null
    private val binding get() = _binding!!

    private var _licenseManager: LicenseManager? = null
    private val licenseManager get() = _licenseManager!!

    private var _licenseAdapter: LicenseAdapter? = null
    private val licenseAdapter get() = _licenseAdapter!!

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        _binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setup()
    }

    private fun setup() {
        _licenseManager = LicenseManager()
        _licenseAdapter = LicenseAdapter(this@LicenseActivity, licenseManager.getLicenses())

        binding.licensesRecyclerView.adapter = licenseAdapter
    }

    override fun onDestroy() {
        super.onDestroy()

        _licenseManager = null
        _licenseAdapter = null
        _binding = null
    }
}