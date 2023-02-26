package tw.app.hotshots.activity.posts

import android.animation.Animator
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
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

    /**
     * The [value] boolean should be True if input should be enabled or False when disabled
     */
    fun toggleViewPagerInput(value: Boolean) {
        binding.imagesViewPager.isUserInputEnabled = value

        if (!value) {
            binding.viewpagerIndicator.animate()
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .alpha(0f)
                .setListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        binding.viewpagerIndicator.visibility = GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })
                .start()
        } else {
            binding.viewpagerIndicator.visibility = VISIBLE
            binding.viewpagerIndicator.animate()
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .alpha(1f)
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _adapter = null
        _post = null
    }
}