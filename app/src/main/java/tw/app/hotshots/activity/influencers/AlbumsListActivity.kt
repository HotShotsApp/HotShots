package tw.app.hotshots.activity.influencers

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.R
import tw.app.hotshots.database.influencers.GetInfluencers
import tw.app.hotshots.database.influencers.SingleInfluencerListener
import tw.app.hotshots.databinding.ActivityAlbumsListBinding
import tw.app.hotshots.fragment.AlbumsFragment
import tw.app.hotshots.model.main.Influencer
import tw.app.hotshots.ui.loading.LoadingDialog
import kotlin.coroutines.CoroutineContext

class AlbumsListActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: ActivityAlbumsListBinding? = null
    private val binding get() = _binding!!

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAlbumsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _loading = LoadingDialog(this@AlbumsListActivity)
        loading.build()
        loading.show()

        launch {
            GetInfluencers().byUid(intent.getStringExtra("uid")!!, object : SingleInfluencerListener {
                override fun onReceived(influencer: Influencer) {
                    runOnUiThread {
                        loading.dismiss()
                        supportFragmentManager.beginTransaction()
                            .add(R.id.fragment_container, AlbumsFragment.newInstance(influencer) as Fragment)
                            .commit()
                    }
                }

                override fun onError(reason: String) {
                    runOnUiThread {
                        Toast.makeText(this@AlbumsListActivity, reason, Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}