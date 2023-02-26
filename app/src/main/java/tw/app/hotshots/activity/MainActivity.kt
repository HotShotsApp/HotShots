package tw.app.hotshots.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.app.HotShots
import tw.app.hotshots.R
import tw.app.hotshots.activity.notifications.NotificationsActivity
import tw.app.hotshots.databinding.ActivityMainBinding
import tw.app.hotshots.databinding.ContentMainBinding
import tw.app.hotshots.settings.SettingsActivity
import tw.app.hotshots.storage.Upload
import tw.app.hotshots.storage.UploadListener
import tw.app.hotshots.ui.link.CreateLinkDialog
import tw.app.hotshots.ui.link.CreateLinkDialog.Companion.CreateLinkDialogListener
import tw.app.hotshots.ui.loading.LoadingDialog
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var _content: ContentMainBinding? = null
    private val content get() = _content!!

    private lateinit var debugService: Intent

    private var _loadingDialog: LoadingDialog? = null
    private val loadingDialog get() = _loadingDialog!!

    private var _createLinkDialog: CreateLinkDialog? = null
    private val createLinkDialog get() = _createLinkDialog!!

    private var _createLinkListener: CreateLinkDialogListener? = null
    private val createLinkListener get() = _createLinkListener!!

    var shouldUploadToDatabase = true

    // Avatar Picker
    var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var listenerAvatar: AvatarPickerListener? = null
    private val uriPermissionFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, result.uriContent);

            listenerAvatar!!.onCropped(bitmap)

            if (shouldUploadToDatabase) {
                launch {
                    Upload().Bitmap(
                        "avatars",
                        bitmap,
                        object : UploadListener {
                            override fun onUploaded(fileUrl: String) {
                                listenerAvatar!!.onUploaded(fileUrl)
                            }

                            override fun onError(reason: String) {
                                listenerAvatar!!.onUploadError(reason)
                            }
                        }
                    )
                }
            } else {
                shouldUploadToDatabase = true
            }
        } else {
            listenerAvatar!!.onUploadError(result.error?.message!!)
        }
    }

    init {
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    contentResolver.takePersistableUriPermission(uri, uriPermissionFlag)

                    cropImage.launch(
                        options(uri = uri) {
                            setGuidelines(CropImageView.Guidelines.ON)
                            setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        }
                    )
                }
            }
    }

    private fun setApplicationCurrentActivity() {
        val myApp = applicationContext as HotShots
        myApp.setCurrentActivity(this@MainActivity)
    }

    override fun onResume() {
        setApplicationCurrentActivity()
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setApplicationCurrentActivity()

        buildLoadingDialog()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        _content = binding.mainContentInclude
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        content.navigationBottomView.setupWithNavController(navController)
        content.railNavigationBottomView.setupWithNavController(navController)

        setup()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _content = null
        _loadingDialog = null
        _createLinkDialog = null
        _createLinkListener = null
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

            R.id.notifications_button -> {
                val intent = Intent(this@MainActivity, NotificationsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

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
        _loadingDialog = LoadingDialog(this@MainActivity)
        loadingDialog.build()
        loadingDialog.setIndeterminate(true)
    }

    fun toggleLoading(on: Boolean): LoadingDialog {
        if (on)
            loadingDialog.show()
        else
            loadingDialog.dismiss()

        return loadingDialog
    }

    fun setAvatarPickerListener(_listener: AvatarPickerListener) {
        listenerAvatar = _listener
    }
}

interface AvatarPickerListener {
    fun onCropped(croppedBitmap: Bitmap) {}

    fun onUploaded(fileUrl: String) {}

    fun onUploadError(error: String) {}
}