package tw.app.hotshots.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tw.app.hotshots.R
import tw.app.hotshots.databinding.FragmentMainBinding
import tw.app.hotshots.ui.button.TopButtonView
import tw.app.hotshots.ui.dialog.loading.LoadingDialog

class MainFragment : BaseFragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    private var topViewButtonActions = TopViewButtonActions.REFRESH

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
        init()
    }

    private fun setup() {
        // Loading Dialog
        _loading = LoadingDialog(requireContext())
        loading.build()
    }

    private fun init() {
        binding.topButtonView.setOnClickListener(object : TopButtonView.ClickListener { // TODO: Change to reference
            override fun onClick() {
                when(topViewButtonActions) {
                    TopViewButtonActions.REFRESH -> {
                        // TODO: Refresh list with posts
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        _loading = null
    }

    public enum class TopViewButtonActions {
        REFRESH
    }
}