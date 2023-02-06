package tw.app.hotshots.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.albums.CreateAlbumActivity
import tw.app.hotshots.adapter.recyclerview.AlbumsAdapter
import tw.app.hotshots.database.albums.GetAlbums
import tw.app.hotshots.database.albums.OnGetAlbumsListener
import tw.app.hotshots.database.influencers.FollowInfluencer
import tw.app.hotshots.database.influencers.FollowSettings
import tw.app.hotshots.database.influencers.OnFollowListener
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.databinding.FragmentAlbumsBinding
import tw.app.hotshots.model.main.Album
import tw.app.hotshots.model.main.Influencer
import tw.app.hotshots.ui.loading.LoadingDialog
import tw.app.hotshots.R.string as AppString
import kotlin.coroutines.CoroutineContext

class AlbumsFragment(
    private var influencer: Influencer
) : Fragment(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    private var _adapter: AlbumsAdapter? = null
    private val adapter get() = _adapter!!

    private var _layoutManager: GridLayoutManager? = null
    private val layoutManager get() = _layoutManager!!

    private var _albumsData: MutableList<Album>? = arrayListOf()
    private val albumsData get() = _albumsData!!

    private var _loadingDialog: LoadingDialog? = null
    private val loadingDialog get() = _loadingDialog!!

    private var isUserFollows = false
    private var followsCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _adapter = null
        _layoutManager = null
        _albumsData = null
        _loadingDialog = null
    }

    private fun setup() {
        _loadingDialog = LoadingDialog(requireContext())
        _loadingDialog!!.build()

        binding.profileAvatarView.setAvatarFromUrl(influencer.avatar)
        binding.profileUsernameText.text = influencer.nickname
        binding.profileDescriptionText.text = influencer.description
        followsCount = influencer.follows.size

        binding.followButton.text = "${getString(AppString.follow_count)} ($followsCount)"
        for (follow in influencer.follows) {
            if (follow.followedBy == UserSingleton.instance?.user?.uid!!) {
                isUserFollows = true
                binding.followButton.text = "${getString(AppString.you_follow_count)} ($followsCount)"
            }
        }

        val onFollowListener = object : OnFollowListener {
            override fun onFollow() {
                requireActivity().runOnUiThread {
                    loadingDialog.dismiss()
                    isUserFollows = true
                    followsCount += 1
                    setFollowCount()
                }
            }

            override fun onUnFollow() {
                requireActivity().runOnUiThread {
                    loadingDialog.dismiss()
                    isUserFollows = false
                    followsCount -= 1
                    setFollowCount()
                }
            }

            override fun onError(reason: String) {
                requireActivity().runOnUiThread {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.followButton.setOnClickListener {
        loadingDialog.show()
            if (isUserFollows) {
                launch {
                    FollowInfluencer().invoke(influencer.uid, FollowSettings.UNFOLLOW, onFollowListener)
                }
            } else {
                launch {
                    FollowInfluencer().invoke(influencer.uid, FollowSettings.FOLLOW, onFollowListener)
                }
            }
        }

        loadingDialog.show()
        loadAlbumsFromDatabase()

        binding.addButton.setOnClickListener {
            var intent = Intent(requireActivity(), CreateAlbumActivity::class.java)
            intent.putExtra("uid", influencer.uid)
            startActivity(intent)
        }
    }

    private fun setFollowCount() {
        if (isUserFollows) {
            binding.followButton.text = "${getString(AppString.you_follow_count)} (${followsCount})"
        } else {
            binding.followButton.text = "${getString(AppString.follow_count)} ($followsCount)"
        }
    }

    private fun loadIntoRecyclerView() {
        requireActivity().runOnUiThread {
            _layoutManager = GridLayoutManager(requireContext(), 3)
            _adapter = AlbumsAdapter(albumsData)

            binding.albumsRecyclerView.layoutManager = layoutManager
            binding.albumsRecyclerView.adapter = adapter

            loadingDialog.dismiss()
        }
    }

    private fun loadAlbumsFromDatabase() {
        launch {
            GetAlbums().invoke(influencer.uid, object : OnGetAlbumsListener {
                override fun onReceived(albums: MutableList<Album>) {
                    _albumsData = albums

                    loadIntoRecyclerView()
                }

                override fun onError(reason: String) {

                }
            })
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param influencer Influencer to show
         * @return A new instance of fragment AlbumsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(influencer: Influencer) =
            AlbumsFragment(influencer)
    }
}