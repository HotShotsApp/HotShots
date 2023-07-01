package tw.app.hotshots.activity.auth.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.database.nicknames.NickCheckListener
import tw.app.hotshots.database.nicknames.NickNames
import tw.app.hotshots.databinding.FragmentAuthBinding
import tw.app.hotshots.extensions.hideKeyboard
import tw.app.hotshots.fragment.BaseFragment

class AuthFragment : BaseFragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private var _authentication: Authentication? = null
    private val authentication get() = _authentication!!

    private var _authenticationListener: AuthenticationListener? = null
    private val authenticationListener get() = _authenticationListener!!

    private var _authActivity: AuthActivity? = null
    private val authActivity get() = _authActivity!!

    private var isRegister = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()

        _authActivity = null
        _authentication = null
        _authenticationListener = null
    }

    private fun setup() {
        _authActivity = activity as AuthActivity

        _authenticationListener = object : AuthenticationListener {
            override fun onRegistered(user: HotUser) {
                super.onRegistered(user)

                authActivity.runOnUiThread {
                    if (authActivity._user == null)
                        authActivity._user = user

                    authActivity.nextFragment()
                }
            }

            override fun onLogin(user: HotUser) {
                super.onLogin(user)

                authActivity.runOnUiThread {
                    if (!user.isBanned) {
                        authActivity.moveToMainActivity()
                    }
                }
            }

            override fun onError(exception: Exception) {
                super.onError(exception)

                authActivity.runOnUiThread {
                    binding.emailEditText.hideKeyboard()
                    binding.usernameEditText.hideKeyboard()
                    binding.passwordEditText.hideKeyboard()

                    showSnackbar(exception.message.toString())
                }
            }
        }

        _authentication = Authentication(authenticationListener)
    }

    private fun init() {
        binding.doneButton.setOnClickListener {
            if (validate()) {
                launch {
                    if (isRegister) {
                        NickNames().checkIfNicknameAvailable(binding.usernameEditText.text.toString(), object : NickCheckListener {
                            override fun onAvailable() {
                                requireActivity().runOnUiThread {
                                    binding.usernameTextInputLayout.error = ""
                                    binding.usernameTextInputLayout.isErrorEnabled = false
                                }

                                launch {
                                    authentication.register(
                                        binding.emailEditText.text.toString(),
                                        binding.usernameEditText.text.toString(),
                                        binding.passwordEditText.text.toString()
                                    )
                                }
                            }

                            override fun onTaken() {
                                requireActivity().runOnUiThread {
                                    binding.usernameTextInputLayout.error = "Nazwa jest już zajęta!"
                                    binding.usernameTextInputLayout.isErrorEnabled = true
                                }
                            }

                            override fun onError(exception: Exception) {
                                requireActivity().runOnUiThread {
                                    Snackbar.make(
                                        binding.root,
                                        "Wystąpił błąd: ${exception.message ?: "nieznany błąd"} - Spróbuj ponownie!",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                    } else {
                        authentication.login(
                            binding.emailEditText.text.toString(),
                            binding.passwordEditText.text.toString()
                        )
                    }
                }
            } else {
                toggleInput(true)
            }
        }
    }

    private fun validate(): Boolean {
        toggleInput(false)

        binding.emailEditText.hideKeyboard()
        binding.usernameEditText.hideKeyboard()
        binding.passwordEditText.hideKeyboard()

        if (!binding.emailEditText.text.toString().isValidEmail()) {
            showSnackbar("Email jest nieprawidłowy!")
            return false
        }

        if (isRegister && !binding.usernameEditText.text.toString().matches(Regex("[a-zA-Z0-9_.-]{4,}"))) {
            showSnackbar("Nazwa może zawierać a-z, A-Z, 0-9, \"_\", \".\", \"-\" i musi mieć conajmniej 4 znaki!")
        }

        if (binding.passwordEditText.text.toString().length < 8) {
            showSnackbar("Hasło jest za krótkie!")
        }

        if (binding.passwordEditText.text.toString().length >= 32) {
            showSnackbar("Hasło jest za długie!")
        }

        return true
    }

    private fun toggleInput(isEnabled: Boolean) {
        binding.emailEditText.isEnabled = isEnabled
        binding.usernameEditText.isEnabled = isEnabled
        binding.passwordEditText.isEnabled = isEnabled

        binding.doneButton.isEnabled = isEnabled
        binding.changeButton.isEnabled = isEnabled

        binding.loadingIndicator.isVisible = !isEnabled
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun CharSequence?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}