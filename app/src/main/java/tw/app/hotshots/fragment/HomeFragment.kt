package tw.app.hotshots.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.R
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.activity.posts.CreatePostActivity
import tw.app.hotshots.adapter.recyclerview.PostsAdapter
import tw.app.hotshots.database.posts.GetPosts
import tw.app.hotshots.database.posts.GetPostsSettings
import tw.app.hotshots.database.posts.PostsListener
import tw.app.hotshots.databinding.FragmentHomeBinding
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.model.main.Post
import kotlin.coroutines.CoroutineContext


/**
 * A simple [Fragment] subclass
 * Shows most important things that user wanna see after opening app.
 */
class HomeFragment : Fragment(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */


    private val TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mainActivity = (activity as MainActivity)

        Logger.log(
            "$TAG | onCreateView",
            LogType.NORMAL
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.toggleLoading(true)

        val isNotOnPhone = resources.getBoolean(R.bool.isTablet)
        if (isNotOnPhone) {
            setGridLayoutManager()
        } else {
            val display: Display = activity?.windowManager?.defaultDisplay!!
            val orientation: Int = display.getOrientation()

            when (orientation) {
                Surface.ROTATION_90, Surface.ROTATION_270 -> {
                    setGridLayoutManager()
                }
                Surface.ROTATION_0, Surface.ROTATION_180 -> {
                    setLinearLayoutManager()
                }
            }
        }

        val postsSettings = GetPostsSettings()
            .setSearchAllPosts()

        launch {
            GetPosts.invoke(postsSettings, object : PostsListener {
                override fun onReceived(posts: MutableList<Post>) {
                    if (posts.isNotEmpty()) {
                        val fragManager = mainActivity.supportFragmentManager
                        val lifecycle = mainActivity.lifecycle
                        val adapter = PostsAdapter(posts, requireContext(), fragManager, lifecycle)

                        mainActivity.runOnUiThread {
                            binding.postsMainRecyclerView.adapter = adapter
                            mainActivity.toggleLoading(false)
                        }
                    }
                }

                override fun onError(e: Exception) {
                    super.onError(e)
                    mainActivity.runOnUiThread {
                        Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            })
        }

        binding.createButton.setOnClickListener {
            val intent = Intent(mainActivity, CreatePostActivity::class.java)
            startActivity(intent)
            mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Keep this on bottom
        Logger.log(
            "$TAG | onViewCreated",
            LogType.NORMAL
        )
    }

    private fun setLinearLayoutManager() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.postsMainRecyclerView.layoutManager = layoutManager
    }

    private fun setGridLayoutManager() {
        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.postsMainRecyclerView.layoutManager = layoutManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Keep this on bottom
        Logger.log(
            "$TAG | onDestroyView",
            LogType.NORMAL
        )
    }
}