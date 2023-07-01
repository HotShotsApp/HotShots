package tw.app.hotshots.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.HotUser
import java.lang.Exception

class RoutingActivity : BaseActivity() {

    private var _authentication: Authentication? = null
    private val authentication get() = _authentication!!

    private var _authenticationListener: AuthenticationListener? = null
    private val authenticationListener get() = _authenticationListener!!

    private var _nextPageIntent: Intent? = null
    private val nextPageIntent get() = _nextPageIntent!!

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { true }

        setup()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()

        _authentication = null
        _authenticationListener = null
        _nextPageIntent = null
    }

    private fun setup() {
        _authenticationListener = object : AuthenticationListener {
            override fun onAlreadyAuthenticated(user: HotUser) {
                super.onAlreadyAuthenticated(user)

                runOnUiThread {
                    redirect(NEXT_PAGE.MAIN_ACTIVITY)
                }
            }

            override fun onAuthenticationNeeded() {
                super.onAuthenticationNeeded()

                runOnUiThread {
                    redirect(NEXT_PAGE.AUTH_ACTIVITY)
                }
            }

            override fun onError(exception: Exception) {
                super.onError(exception)

                runOnUiThread {

                }
            }
        }

        launch {
            _authentication = Authentication(authenticationListener).splashCheck()
        }
    }

    private fun init() {

    }

    private fun redirect(nextPage: NEXT_PAGE) {
        _nextPageIntent = when (nextPage) {
            NEXT_PAGE.MAIN_ACTIVITY -> {
                Intent(this@RoutingActivity, MainActivity::class.java)
            }

            NEXT_PAGE.AUTH_ACTIVITY -> {
                Intent(this@RoutingActivity, AuthActivity::class.java)
            }
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(nextPageIntent)
        finish()
    }

    private enum class NEXT_PAGE {
        MAIN_ACTIVITY,
        AUTH_ACTIVITY
    }
}