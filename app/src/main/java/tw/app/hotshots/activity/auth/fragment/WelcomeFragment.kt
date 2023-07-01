package tw.app.hotshots.activity.auth.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.databinding.FragmentWelcomeBinding
import tw.app.hotshots.fragment.BaseFragment

class WelcomeFragment : BaseFragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private var _authActivity: AuthActivity? = null
    private val authActivity get() = _authActivity!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        setup()
        authActivity.changeNextButtonToDoneButton(false)
    }

    private fun setup() {
        if (activity is AuthActivity)
            _authActivity = activity as AuthActivity

        if (_authActivity != null) {
            binding.usernameTextView.text = authActivity._user!!.name
            binding.skipButton.setOnClickListener {
                authActivity.moveToMainActivity()
            }
        } else {
            Snackbar.make(
                binding.root,
                "Coś poszło nie tak! Uruchom aplikacje ponownie!",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }
    }
}