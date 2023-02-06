package tw.app.hotshots.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.BuildConfig
import tw.app.hotshots.HotShots
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.activity.auth.UnlockActivity
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.user.UserDatabase
import tw.app.hotshots.database.user.UserDatabaseListener
import tw.app.hotshots.databinding.ActivitySplashBinding
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.R.string as AppString
import java.util.*
import kotlin.coroutines.CoroutineContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    /* ------------------------------------------ */

    private val TAG = "SplashActivity"

    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!

    private var _authentication: Authentication? = null
    private val authentication get() = _authentication!!

    private var _authenticationListener: AuthenticationListener? = null
    private val authenticationListener get() = _authenticationListener!!

    private var _noticeDialog: MaterialAlertDialogBuilder? = null
    private val noticeDialog get() = _noticeDialog!!

    private var _bannedDialog: MaterialAlertDialogBuilder? = null
    private val bannedDialog get() = _bannedDialog!!

    private var _unbannedDialog: MaterialAlertDialogBuilder? = null
    private val unbannedDialog get() = _unbannedDialog!!

    private var _settings: Settings? = null
    private val settings get() = _settings!!

    // For buildDialogs()
    private var user: User? = null
    private var bannedMessage = ""
    private var isPinRequired = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setApplicationCurrentActivity()

        initialize()

        setup()

        Logger.log(
            "$TAG | onCreate",
            LogType.NORMAL
        )
    }

    private fun setApplicationCurrentActivity() {
        val myApp = applicationContext as HotShots
        myApp.setCurrentActivity(this@SplashActivity)
    }

    override fun onResume() {
        super.onResume()
        setApplicationCurrentActivity()
    }

    private fun initialize() {
        buildDialogs()

        _authenticationListener = object : AuthenticationListener {
            override fun onUserAlreadyLogged(_user: User) {
                super.onUserAlreadyLogged(_user)
                user = _user

                Log.d(TAG, "onUserAlreadyLogged: ispinenabled: ${_user.isPinEnabled}")

                isPinRequired = _user.isPinEnabled

                if (_user.isBanned) {
                    if (_user.bannedTo > TimeUtil.currentTimeToLong()) { // Is still banned
                        bannedMessage = "Twoje konto zostało zbanowane!\n\nPowód:\n" +
                                _user.banReason + "\n\n" + "Zakończy się dnia:\n" +
                                TimeUtil.convertLongToTime(_user.bannedTo)
                        bannedDialog.show()
                    } else if (_user.bannedTo <= TimeUtil.currentTimeToLong()) {
                        unbannedDialog.show()
                    }

                    return
                }

                if (_user.isPinEnabled) {
                    moveToUnlock()
                } else {
                    moveToHome()
                }
            }

            override fun onUserAuthenticationNeeded() {
                super.onUserAuthenticationNeeded()

                if (settings.isUserFirstRun()) {
                    noticeDialog.show()
                } else {
                    moveToAuth()
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)

                showError(e.message.toString())
            }
        }

        _authentication = Authentication(authenticationListener)
        launch {
            Logger.log(
                "$TAG | Started Authentication Initialize",
                LogType.NORMAL
            )
            authentication.initialize()
        }

        Logger.log(
            "$TAG | Initialized",
            LogType.NORMAL
        )
    }

    private fun setup() {
        val ver = if (BuildConfig.DEBUG) {
            "${BuildConfig.VERSION_NAME}\n${BuildConfig.BUILD_TYPE.uppercase(Locale.getDefault())}"
        } else {
            BuildConfig.VERSION_NAME
        }

        binding.appVersionText.text = ver

        Logger.log(
            "$TAG | Setup",
            LogType.NORMAL
        )
    }

    private fun buildDialogs() {
        // Notice Dialog
        _noticeDialog = MaterialAlertDialogBuilder(this@SplashActivity)
        noticeDialog.setTitle(getString(AppString.notice_dialog_startup_title))
        noticeDialog.setMessage(getString(AppString.notice_dialog_startup_message))
        noticeDialog.setPositiveButton(getString(AppString.notice_dialog_startup_positive_button)) { _, _ ->
            moveToAuth()
        }
        noticeDialog.setOnDismissListener {
            moveToAuth()
        }

        // Banned Dialog
        _bannedDialog = MaterialAlertDialogBuilder(this@SplashActivity)
        bannedDialog.setTitle(getString(AppString.banned_dialog_title))
        bannedDialog.setMessage(bannedMessage)
        bannedDialog.setPositiveButton(getString(AppString.banned_dialog_positive_button)) { _, _ ->
            finishAffinity()
        }
        bannedDialog.setOnDismissListener {
            finishAffinity()
        }

        // Unbanned Dialog
        _unbannedDialog = MaterialAlertDialogBuilder(this@SplashActivity)
        unbannedDialog.setTitle(getString(AppString.unbanned_dialog_title))
        unbannedDialog.setMessage(getString(AppString.unbanned_dialog_message))
        unbannedDialog.setPositiveButton(getString(AppString.unbanned_dialog_positive_button)) { _, _ ->
            unbanAndMove();
        }
        unbannedDialog.setOnDismissListener {
            unbanAndMove();
        }
    }

    private fun unbanAndMove() {
        user!!.isBanned = false
        user!!.bannedTo = 0

        var userDatabase = UserDatabase(object : UserDatabaseListener {
            override fun onUpdated(user: User) {
                if (isPinRequired)
                    moveToUnlock()
                else
                    moveToHome()
            }

            override fun onError(exception: java.lang.Exception) {
                showError("Uruchom aplikacje ponownie: ${exception.message}")
            }
        })

        launch {
            userDatabase.saveUser(user!!)
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Snackbar.make(
                binding.mainView,
                message,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun moveToUnlock() {
        val intent = Intent(this@SplashActivity, UnlockActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun moveToHome() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun moveToAuth() {
        val intent = Intent(this@SplashActivity, AuthActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        _noticeDialog = null
        _bannedDialog = null
        _unbannedDialog = null
        _settings = null
        _binding = null
        _authentication = null
        _authenticationListener = null
    }
}