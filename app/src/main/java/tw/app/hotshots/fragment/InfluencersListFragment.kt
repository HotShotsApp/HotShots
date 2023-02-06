package tw.app.hotshots.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.activity.influencers.AddInfluencerActivity
import tw.app.hotshots.activity.influencers.AlbumsListActivity
import tw.app.hotshots.adapter.recyclerview.InfluencersAdapter
import tw.app.hotshots.adapter.recyclerview.OnInfluencerClickListener
import tw.app.hotshots.adapter.recyclerview.PostsAdapter
import tw.app.hotshots.database.influencers.GetInfluencers
import tw.app.hotshots.database.influencers.InfluencersListener
import tw.app.hotshots.database.influencers.InfluencersSingleton
import tw.app.hotshots.databinding.FragmentInfluencersListBinding
import tw.app.hotshots.model.main.Influencer
import kotlin.coroutines.CoroutineContext

class InfluencersListFragment : Fragment(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: FragmentInfluencersListBinding? = null
    private val binding get() = _binding!!

    private var _mainActivity: MainActivity? = null
    private val mainActivity get() = _mainActivity!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentInfluencersListBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _mainActivity = requireActivity() as MainActivity

        setup();
        setupOnRefresh()
        loadInfluencersFromSingleton()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _mainActivity = null
    }

    private fun setup() {
        binding.addButton.setOnClickListener {
            var intent = Intent(requireActivity(), AddInfluencerActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupOnRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadInfluencersFromDatabase()
        }
    }

    private fun toggleRefresh(on: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = on
    }

    private fun loadIntoRecycler(list: MutableList<Influencer>) {
        var layoutManager = LinearLayoutManager(requireContext())
        var adapter = InfluencersAdapter(list, requireContext(), object : OnInfluencerClickListener {
            override fun onClick(influencer: Influencer) {
                val intent = Intent(requireActivity(), AlbumsListActivity::class.java)
                intent.putExtra("uid", influencer.uid)
                startActivity(intent)
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })

        binding.influencersRecyclerView.layoutManager = layoutManager
        binding.influencersRecyclerView.adapter = adapter
    }

    private fun loadInfluencersFromDatabase() {
        launch {
            GetInfluencers().invoke(object : InfluencersListener {
                override fun onReceived(influencers: MutableList<Influencer>) {
                    mainActivity.runOnUiThread {
                        loadIntoRecycler(influencers)
                        mainActivity.toggleLoading(false)
                        toggleRefresh(false)
                    }
                }

                override fun onError(reason: String) {
                    mainActivity.runOnUiThread {
                        Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show()
                        toggleRefresh(false)
                    }
                }
            })
        }
    }

    private fun loadInfluencersFromSingleton() {
        var instanceOfInfluencers = InfluencersSingleton.Instance

        if (instanceOfInfluencers.getStoredInfluencers().isNotEmpty()) {
            loadIntoRecycler(instanceOfInfluencers.getStoredInfluencers())
        } else {
            mainActivity.toggleLoading(true)
            loadInfluencersFromDatabase()
        }
    }
}