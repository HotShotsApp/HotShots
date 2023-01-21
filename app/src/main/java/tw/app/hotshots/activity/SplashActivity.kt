package tw.app.hotshots.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.BuildConfig
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.activity.auth.UnlockActivity
import tw.app.hotshots.activity.debug.DebugService
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.databinding.ActivitySplashBinding
import tw.app.hotshots.encryption.Encrypt
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
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

    private var authentication: Authentication? = null
    private var authenticationListener: AuthenticationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        setup()

        Logger.log(
            "$TAG | onCreate",
            LogType.NORMAL
        )


    }

    private fun initialize() {
        authenticationListener = object : AuthenticationListener {
            override fun onUserAlreadyLogged(user: User) {
                super.onUserAlreadyLogged(user)

                Log.d(TAG, "onUserAlreadyLogged: ispinenabled: ${user.isPinEnabled}")
                
                if (user.isPinEnabled) {
                    moveToUnlock()
                } else {
                    moveToHome()
                }
            }

            override fun onUserAuthenticationNeeded() {
                super.onUserAuthenticationNeeded()

                moveToAuth()
            }

            override fun onError(e: Exception) {
                super.onError(e)

                showError(e.message.toString())
            }
        }

        authentication = Authentication(authenticationListener)
        launch {
            Logger.log(
                "$TAG | Started Authentication Initialize",
                LogType.NORMAL
            )
            authentication?.initialize()
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

        _binding = null
    }
}