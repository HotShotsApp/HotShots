package tw.app.hotshots.activity.auth

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.database.posts.user.UserSingleton
import tw.app.hotshots.databinding.ActivityUnlockBinding
import tw.app.hotshots.encryption.Encrypt
import tw.app.hotshots.extensions.hideKeyboard
import tw.app.hotshots.util.Vibrate

class UnlockActivity : AppCompatActivity() {
    
    private val TAG = "UnlockApp"

    private var _binding: ActivityUnlockBinding? = null
    private val binding get() = _binding!!

    lateinit var userPin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setup()
    }

    private fun init() {
        userPin = UserSingleton.instance?.user?.pin!!
        Log.d(TAG, "init: User Hashed PIN: $userPin")
    }

    private fun setup() {
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