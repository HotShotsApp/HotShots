package tw.app.hotshots.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import tw.app.hotshots.R
import tw.app.hotshots.adapter.recycler.PostAdapter
import tw.app.hotshots.database.posts.ListenPosts
import tw.app.hotshots.database.posts.ListenPostsListener
import tw.app.hotshots.database.posts.model.Post
import tw.app.hotshots.databinding.FragmentMainBinding
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.ui.button.TopButtonView
import tw.app.hotshots.ui.dialog.loading.LoadingDialog

class MainFragment : BaseFragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    private var _listenPostsListener: ListenPostsListener? = null
    private val listenPostsListener get() = _listenPostsListener!!

    private var _adapter: PostAdapter? = null
    private val adapter get() = _adapter!!

    private var topViewButtonActions = TopViewButtonActions.REFRESH

    private var _settings: Settings? = null
    private val settings get() = _settings!!

    private var _morePostsList: MutableList<Post>? = null
    private val morePostsList get() = _morePostsList!!

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
        _settings = Settings(PreferenceManager.getDefaultSharedPreferences(requireContext()))

        // Loading Dialog
        _loading = LoadingDialog(requireContext())
        loading.build()
    }

    private fun init() {
        binding.topButtonView.setOnClickListener(object : TopButtonView.ClickListener { // TODO: Change to reference
            override fun onClick() {
                when(topViewButtonActions) {
                    TopViewButtonActions.REFRESH -> {
                        adapter.addPostsToList(morePostsList)
                        binding.topButtonView.hide()
                    }
                }
            }
        })
    }

    private fun setupAdapterLoadPosts() {
        if (_listenPostsListener == null) {
            _listenPostsListener = object : ListenPostsListener {
                override fun onFullReceived(posts: MutableList<Post>) {
                    requireActivity().runOnUiThread {
                        if (posts.isEmpty()) {
                            Snackbar.make(
                                binding.root,
                                "Brak postów do pokazania!",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            _adapter = PostAdapter(
                                posts,
                                settings
                            )

                            binding.postsRecyclerView.adapter = adapter
                        }
                    }
                }

                override fun onNewPostsAdded(newPosts: MutableList<Post>) {
                    requireActivity().runOnUiThread {
                        _morePostsList = newPosts
                        binding.topButtonView.setText("${newPosts.size} nowe posty!")
                        binding.topButtonView.show()
                    }
                }

                override fun onError(exception: Exception) {
                    requireActivity().runOnUiThread {
                        Snackbar.make(
                            binding.root,
                            exception.message ?: "Unknown error!",
                            Snackbar.LENGTH_INDEFINITE
                        ).show()
                    }
                }
            }

            launch {
                ListenPosts().invoke(listenPostsListener)
            }
        }
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