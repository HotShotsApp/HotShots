package tw.app.hotshots.adapter.viewpager.post.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.ortiz.touchview.OnTouchImageViewListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import tw.app.hotshots.R
import tw.app.hotshots.activity.posts.BigPictureSlideActivity
import tw.app.hotshots.databinding.FragmentBigPictureImageBinding
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.Constants

/**
 * Simple browser class
 * --------------------
 *
 * TODO: Better security / better experience
 */

class BigPictureImageFragment(
    private val imageUrl: String
) : Fragment() {
    private var _binding: FragmentBigPictureImageBinding? = null
    private val binding get() = _binding!!

    private val settings = Settings.getInstance

    private val TAG = "BigPostZoomControl"

    private var isZoomedOnMove = false
    private var isZoomedOnDoubleTap = false
    private var isAlreadyZoomed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBigPictureImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Picasso
            .get()
            .load(imageUrl)
            .into(binding.postImage, object : Callback {
                override fun onSuccess() {
                    setupZoomControl()
                    binding.postImage.visibility = View.VISIBLE
                    binding.loadingImageProgress.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    val errorMessage = if (e == null)
                        "Wystąpił nieznany błąd"
                    else
                        "Błąd: ${e.message}"

                    setError(errorMessage)
                }
            })
    }

    private fun setError(error: String) {
        binding.errorText.text = error
        binding.errorText.visibility = View.VISIBLE
        binding.loadingImageProgress.visibility = View.GONE
        binding.postImage.visibility = View.GONE
    }

    private fun setupZoomControl() {
        binding.postImage.setOnTouchImageViewListener(object : OnTouchImageViewListener {
            override fun onMove() {
                isZoomedOnMove = binding.postImage.isZoomed

                Log.d(TAG, "onMove: Triggered is zoomed: $isZoomedOnMove")

                if (isZoomedOnMove && !isAlreadyZoomed) {
                    isAlreadyZoomed = true

                    if (getFragmentActivity() != null)
                        getFragmentActivity()!!.toggleViewPagerInput(false)
                } else if (!isZoomedOnMove && isAlreadyZoomed) {
                    isAlreadyZoomed = false

                    if (getFragmentActivity() != null)
                        getFragmentActivity()!!.toggleViewPagerInput(true)
                }
            }
        })
    }

    private fun getFragmentActivity(): BigPictureSlideActivity? {
        return if (requireActivity() is BigPictureSlideActivity) {
            Log.d(TAG, "getFragmentActivity: SUCCESS! -> requireActivity is BigPictureSlideActivity")

            requireActivity() as BigPictureSlideActivity
        } else {
            Log.d(TAG, "getFragmentActivity: ERROR! -> requireActivity is not BigPictureSlideActivity")

            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}