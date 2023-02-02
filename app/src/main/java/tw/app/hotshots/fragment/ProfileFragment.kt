package tw.app.hotshots.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.adapter.recyclerview.PostsAdapter
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.posts.GetPosts
import tw.app.hotshots.database.posts.GetPostsSettings
import tw.app.hotshots.database.posts.PostsListener
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.databinding.FragmentProfileBinding
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.model.main.Post
import tw.app.hotshots.ui.profile.EditProfileDialog
import tw.app.hotshots.ui.profile.EditProfileListener
import kotlin.coroutines.CoroutineContext
import tw.app.hotshots.R.string as AppString

/**
 * A [Fragment] subclass
 * Shows currently logged in user profile
 * and allows user to edit details about his profile (avatar, profile description)
 */
class ProfileFragment : Fragment(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private val TAG = "ProfileFragment"

    private val postsTabId = 105
    private val albumsTabId = 106

    private var _binding: FragmentProfileBinding? = null
    private var onTabSelectedListener: OnTabSelectedListener? = null

    private var editProfileDialog: EditProfileDialog? = null

    private lateinit var mainActivity: MainActivity

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)

        Logger.log(
            "$TAG | onCreateView",
            LogType.NORMAL
        )

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.toggleLoading(true)

        setupTabLayout()
        setup()

        loadPostsIntoRecyclerView()

        // Keep this on bottom
        Logger.log(
            "$TAG | onViewCreated",
            LogType.NORMAL
        )
    }

    private fun setup() {
        setupEditProfileDialog()

        val currentUser = UserSingleton.instance?.user!!

        binding.profileUsernameText.text = currentUser.name
        binding.profileDescriptionText.text = currentUser.description

        val layoutManager = LinearLayoutManager(requireContext())
        binding.profileRecyclerView.layoutManager = layoutManager

        binding.profileAvatarView.setAvatarFromUrl(currentUser.avatar)
        binding.profileAvatarView.setAvatarLevel(currentUser.accountType)

        binding.profileEditButton.setOnClickListener {
            editProfileDialog!!.show()
        }
    }

    private fun setupEditProfileDialog() {
        editProfileDialog = EditProfileDialog(requireContext(), activity as MainActivity, object : EditProfileListener {
            override fun onEdited(user: User) {
                binding.profileAvatarView.setAvatarFromUrl(user.avatar)
                binding.profileDescriptionText.text = user.description
            }
        })
    }

    /**
     * Setups TabLayout with buttons
     * and adds OnTabChangedListener
     */
    private fun setupTabLayout() {
        val posts = binding.profileTabLayout.newTab()
            .setId(postsTabId)
            .setText(getString(AppString.posts_tablayout))

        val albums = binding.profileTabLayout.newTab()
            .setId(albumsTabId)
            .setText(getString(AppString.albums_tablayout))

        binding.profileTabLayout.addTab(posts)
        binding.profileTabLayout.addTab(albums)

        onTabSelectedListener = object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabSelected(tab?.id!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        }

        binding.profileTabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    /**
     * Handle on tab selected item change
     * If posts clicked - load posts etc.
     */
    private fun onTabSelected(tabId: Int) {
        when (tabId) {
            postsTabId -> {
                loadPostsIntoRecyclerView()
            }

            albumsTabId -> {
                loadAlbumsIntoRecyclerView()
            }
        }
    }

    /**
     * Load's user created posts into RecyclerView
     */
    private fun loadPostsIntoRecyclerView() {
        val postsSettings = GetPostsSettings()
            .setSearchUserPosts(FirebaseAuth.getInstance().currentUser?.uid!!)

        launch {
            GetPosts.invoke(postsSettings, object : PostsListener {
                override fun onReceived(posts: MutableList<Post>) {
                    if (posts.isNotEmpty()) {
                        val fragManager = mainActivity.supportFragmentManager
                        val lifecycle = mainActivity.lifecycle
                        val adapter = PostsAdapter(posts, requireContext(), fragManager, lifecycle)

                        mainActivity.runOnUiThread {
                            binding.profileRecyclerView.adapter = adapter
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
    }

    /**
     * Load's user created albums into RecyclerView
     */
    private fun loadAlbumsIntoRecyclerView() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.profileTabLayout.removeOnTabSelectedListener(onTabSelectedListener)
        onTabSelectedListener = null

        _binding = null

        // Keep this on bottom
        Logger.log(
            "$TAG | onDestroyView",
            LogType.NORMAL
        )
    }
}