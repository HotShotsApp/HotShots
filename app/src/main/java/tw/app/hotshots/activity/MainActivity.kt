package tw.app.hotshots.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.torrydo.floatingbubbleview.FloatingBubbleService
import tw.app.hotshots.R
import tw.app.hotshots.activity.debug.DebugService
import tw.app.hotshots.activity.posts.CreatePostActivity
import tw.app.hotshots.databinding.ActivityMainBinding
import tw.app.hotshots.databinding.ContentMainBinding
import tw.app.hotshots.settings.SettingsActivity
import tw.app.hotshots.ui.link.CreateLinkDialog
import tw.app.hotshots.ui.link.CreateLinkDialog.Companion.CreateLinkDialogListener
import tw.app.hotshots.ui.loading.LoadingDialog

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var content: ContentMainBinding

    private lateinit var debugService: Intent

    private lateinit var loadingDialog: LoadingDialog
    private var createLinkDialog: CreateLinkDialog? = null
    private var createLinkListener: CreateLinkDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        buildLoadingDialog()

        debugService = Intent(this@MainActivity, DebugService::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        content = binding.mainContentInclude
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        content.navigationBottomView.setupWithNavController(navController)
        content.railNavigationBottomView.setupWithNavController(navController)

        setup()
    }

    private fun setup() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings_button -> {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                true
            }
            R.id.notifications_button -> true
            R.id.debug_button -> {
                if (FloatingBubbleService.isRunning()) {
                    stopService(debugService)
                } else {
                    startService(debugService)

                    if (Build.VERSION.SDK_INT >= 26)
                        startForegroundService(debugService)
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun buildLoadingDialog() {
        loadingDialog = LoadingDialog(this@MainActivity)
        loadingDialog.build()
        loadingDialog.setIndeterminate(true)
    }

    fun toggleLoading(on: Boolean) {
        if (on)
            loadingDialog.show()
        else
            loadingDialog.dismiss()
    }
}