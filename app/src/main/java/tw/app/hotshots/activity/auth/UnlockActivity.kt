package tw.app.hotshots.activity.auth

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.authentication.Biometric
import tw.app.hotshots.authentication.BiometricListener
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.databinding.ActivityUnlockBinding
import tw.app.hotshots.encryption.Encrypt
import tw.app.hotshots.extensions.hideKeyboard
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.Vibrate

class UnlockActivity : AppCompatActivity() {
    
    private val TAG = "UnlockApp"

    private var _binding: ActivityUnlockBinding? = null
    private val binding get() = _binding!!

    private var _settings: Settings? = null
    private val settings get() = _settings!!

    private var _biometric: Biometric? = null
    private val biometric get() = _biometric!!

    private var _biometricListener: BiometricListener? = null
    private val biometricListener get() = _biometricListener!!

    lateinit var userPin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setup()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _settings = null
        _biometric = null
        _biometricListener = null
    }

    private fun init() {
        userPin = UserSingleton.instance?.user?.pin!!
        Log.d(TAG, "init: User Hashed PIN: $userPin")
    }

    private fun setup() {
        _settings = Settings(this)

        if (settings.isBiometricEnabled()) {
            setupBiometric()
        } else {
            setupPinLock()
        }
    }

    private fun setupBiometric() {
        binding.pinUnlockView.visibility = GONE
        binding.bioAuthView.visibility = VISIBLE

        _biometric = Biometric(this, this)
        _biometricListener = object : BiometricListener {
            override fun onAuthenticate() {
                moveToHome()
            }

            override fun onFailed() {
                setupPinLock()
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                setupPinLock()

                // errorCodes:
                // 13 - Canceled by user
                // 10 - Authentication Cancelled (by swipe to background apps, or back button)

                var safeErrors = arrayListOf<Int>(13, 10)
                var isSafeError = false
                for (code in safeErrors) {
                    if (errorCode == code) {
                        isSafeError = true
                        break
                    }
                }

                if (!isSafeError) {
                    Snackbar.make(
                        binding.snackView,
                        "Błąd: $errorCode! $errorMessage",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        biometric.authenticate(biometricListener)
    }

    private fun setupPinLock() {
        binding.pinUnlockView.visibility = VISIBLE
        binding.bioAuthView.visibility = GONE
        binding.otpView.otpListener = object: OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                binding.otpView.hideKeyboard()

                val hashedOtp = Encrypt.encrypt(otp)
                if (hashedOtp == userPin) {
                    moveToHome()
                } else {
                    Vibrate.vibrate(this@UnlockActivity)
                    Snackbar.make(binding.root, "PIN jest nieprawidłowy!", Snackbar.LENGTH_SHORT).show()
                    binding.otpView.setOTP("")
                }
            }
        }
    }

    private fun moveToHome() {
        val intent = Intent(this@UnlockActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}