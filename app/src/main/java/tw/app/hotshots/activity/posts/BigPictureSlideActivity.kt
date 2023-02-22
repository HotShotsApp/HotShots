package tw.app.hotshots.activity.posts

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.adapter.viewpager.post.BigPictureSlideAdapter
import tw.app.hotshots.database.posts.GetPosts
import tw.app.hotshots.database.posts.GetPostsSettings
import tw.app.hotshots.database.posts.PostsListener
import tw.app.hotshots.databinding.ActivityBigPictureSlideBinding
import tw.app.hotshots.model.main.Post
import tw.app.hotshots.ui.loading.LoadingDialog
import kotlin.coroutines.CoroutineContext

class BigPictureSlideActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    /* ------------------------------------------ */

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    private var _binding: ActivityBigPictureSlideBinding? = null
    private val binding get() = _binding!!

    private var _adapter: BigPictureSlideAdapter? = null
    private val adapter get() = _adapter!!

    private var _post: Post? = null
    private val post get() = _post!!

    private var postUid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityBigPictureSlideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _loading = LoadingDialog(this@BigPictureSlideActivity)
        loading.build()
        loading.show()

        getPost()
    }

    private fun getPost() {
        if (intent.getStringExtra("postUid") == null) {
            Toast.makeText(
                this@BigPictureSlideActivity,
                "PostUID jest pusty!\nByć może post został usunięty!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            postUid = intent.getStringExtra("postUid")!!
        }

        launch {
            val postsSettings = GetPostsSettings()
            postsSettings.setSearchByPostUID(postUid)

            GetPosts.invoke(
                postsSettings,
                object : PostsListener {
                    override fun onReceived(post: Post) {
                        super.onReceived(post)

                        _post = post

                        runOnUiThread {
                            loading.dismiss()
                            setup()
                        }
                    }
                }
            )
        }
    }

    private fun setup() {
        _adapter = BigPictureSlideAdapter(
            post.images,
            supportFragmentManager,
            lifecycle
        )

        binding.imagesViewPager.adapter = adapter

        binding.viewpagerIndicator.attachTo(binding.imagesViewPager)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _adapter = null
        _post = null
    }
}