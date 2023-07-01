package tw.app.hotshots.activity.auth.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.rpc.context.AttributeContext.Auth
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.databinding.FragmentDescriptionBinding
import tw.app.hotshots.fragment.BaseFragment

class DescriptionFragment : BaseFragment() {

    private var _binding: FragmentDescriptionBinding? = null
    private val binding get() = _binding!!

    private var _authActivity: AuthActivity? = null
    private val authActivity get() = _authActivity!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
    }

    override fun onResume() {
        super.onResume()

        authActivity.changeNextButtonToDoneButton(true)
        authActivity.isNextButtonNextFragment = false
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _authActivity = null
    }

    private fun setup() {
        if (activity is AuthActivity)
            _authActivity = activity as AuthActivity

        binding.descriptionTextInputEditText.addTextChangedListener {
            authActivity.newDescription = binding.descriptionTextInputEditText.text.toString()
        }
    }
}