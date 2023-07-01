package tw.app.hotshots.activity.auth

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.BaseActivity
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.activity.auth.fragment.AuthFragment
import tw.app.hotshots.activity.auth.fragment.DescriptionFragment
import tw.app.hotshots.activity.auth.fragment.WelcomeFragment
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.database.users.UpdateUser
import tw.app.hotshots.database.users.UpdateUserListener
import tw.app.hotshots.databinding.ActivityAuthBinding
import tw.app.hotshots.ui.dialog.loading.LoadingDialog
import java.lang.Exception

class AuthActivity : BaseActivity() {

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!

    private var _adapter: AuthAdapter? = null
    private val adapter get() = _adapter!!

    private var _moveToMain: Intent? = null
    private val moveToMain get() = _moveToMain!!

    public var _user: HotUser? = null
    private val user get() = _user!!

    public var isNextButtonNextFragment = true
    public var isNextButtonLocked = false

    public var newDescription = ""
    public var newAvatar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _loading = LoadingDialog(this@AuthActivity)
        loading.build()
        loading.setIndeterminate(true)

        _adapter = AuthAdapter(
            supportFragmentManager,
            lifecycle
        )

        adapter.addFragment(AuthFragment())
        adapter.addFragment(WelcomeFragment())
        adapter.addFragment(DescriptionFragment())

        binding.viewPagerAuth.isUserInputEnabled = false
        binding.viewPagerAuth.adapter = adapter

        binding.nextButton.setOnClickListener {
            if (isNextButtonNextFragment)
                nextFragment()
            else {
                if (newDescription.isNotBlank()) {
                    user.description = newDescription
                }

                if (newAvatar.isNotBlank()) {
                    user.avatar = newAvatar
                }

                if (newAvatar.isNotBlank() || newDescription.isNotBlank()) {
                    loading.show()
                    launch {
                        UpdateUser(
                            user,
                            object : UpdateUserListener {
                                override fun onSuccess(user: HotUser) {
                                    super.onSuccess(user)

                                    runOnUiThread {
                                        loading.dismiss()
                                        moveToMainActivity()
                                    }
                                }

                                override fun onError(exception: Exception) {
                                    super.onError(exception)

                                    runOnUiThread {
                                        loading.dismiss()
                                        Snackbar.make(
                                            binding.root,
                                            exception.message!!.toString(),
                                            Snackbar.LENGTH_INDEFINITE
                                        ).show()
                                    }
                                }
                            }
                        ).invoke()
                    }
                } else {
                    moveToMainActivity()
                }
            }
        }
    }

    public fun moveToMainActivity() {
        _moveToMain = Intent(this@AuthActivity, MainActivity::class.java)
        startActivity(moveToMain)
        finish()
    }

    public fun backFragment() {
        if (binding.viewPagerAuth.currentItem != 1) {
            binding.viewPagerAuth.currentItem = binding.viewPagerAuth.currentItem - 1
        }

        checkIfBackButtonShouldBeVisible()
        refreshPageCounter()
    }

    public fun nextFragment() {
        if (!binding.bottomController.isVisible)
            toggleBottomController(true)

        binding.viewPagerAuth.currentItem = binding.viewPagerAuth.currentItem + 1

        binding.nextButton.isEnabled = !isNextButtonLocked
        checkIfBackButtonShouldBeVisible()
        refreshPageCounter()
    }

    private fun checkIfBackButtonShouldBeVisible() {
        binding.backButton.isVisible = binding.viewPagerAuth.currentItem >= 2
    }

    public fun changeNextButtonToDoneButton(shouldChange: Boolean) {
        if (shouldChange) {
            binding.nextButton.text = "Zakończ"
        } else {
            binding.nextButton.text = "Dalej"
        }
    }

    public fun toggleBottomController(isVisible: Boolean) {
        binding.bottomController.isVisible = isVisible
    }

    private fun refreshPageCounter() {
        binding.pageCounterTextView.text =
            "${binding.viewPagerAuth.currentItem} / ${adapter.itemCount - 1}"
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _adapter = null
        _moveToMain = null
        _user = null
    }
}