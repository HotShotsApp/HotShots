package tw.app.hotshots.activity.auth

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.databinding.ActivityAuthBinding
import kotlin.coroutines.CoroutineContext
import tw.app.hotshots.R.string as AppString

class AuthActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    /* ------------------------------------------ */

    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var authentication: Authentication
    private lateinit var authenticationListener: AuthenticationListener

    private var isRegister = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        setup()
    }

    private fun initialize() {

        /*
         * OnAuthenticationNeeded is not needed here
         */
        authenticationListener = object : AuthenticationListener {
            override fun onUserLogin(user: User) {
                super.onUserLogin(user)

                moveToHome()
            }

            override fun onUserRegister(user: User) {
                super.onUserRegister(user)

                moveToHome() // TODO Move new user to some app presentation
            }

            override fun onError(e: Exception) {
                super.onError(e)

                showError(e.message.toString())
            }
        }

        authentication = Authentication(authenticationListener)
        launch {
            authentication.initialize()
        }
    }

    private fun setup() {
        binding.changeButton.setOnClickListener {
            changeState()
        }

        binding.doneButton.setOnClickListener {
            if (isRegister) {
                doRegister()
            } else {
                doLogin()
            }
        }
    }

    private fun doLogin() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        launch {
            authentication.login(email, password)
        }
    }

    private fun doRegister() {
        val email = binding.emailEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        launch {
            authentication.register(email, username, password)
        }
    }

    private fun changeState() {
        if (isRegister) {
            binding.usernameTextInputLayout.visibility = GONE
            binding.doneButton.text = getString(AppString.login_done)
            binding.changeButton.text = getString(AppString.register_change)
        } else {
            binding.usernameTextInputLayout.visibility = VISIBLE
            binding.doneButton.text = getString(AppString.register_done)
            binding.changeButton.text = getString(AppString.login_change)
        }

        isRegister = !isRegister
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

    private fun moveToHome() {
        val intent = Intent(this@AuthActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}