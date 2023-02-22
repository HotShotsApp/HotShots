package tw.app.hotshots.adapter.viewpager.post.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import tw.app.hotshots.R
import tw.app.hotshots.databinding.FragmentBigPictureImageBinding
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.Constants

class BigPictureImageFragment(
    private val imageUrl: String
) : Fragment() {
    private var _binding: FragmentBigPictureImageBinding? = null
    private val binding get() = _binding!!

    private val settings = Settings.getInstance

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

        if (settings.isPrivateViewEnabled()) {
            Picasso
                .get()
                .load(R.drawable.private_image)
                .centerCrop()
                .resize(Constants.POST_IMAGE_RESIZE_VALUE, Constants.POST_IMAGE_RESIZE_VALUE)
                .into(binding.postImage, object : Callback {
                    override fun onSuccess() {
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
        } else {
            Picasso
                .get()
                .load(imageUrl)
                .centerCrop()
                .resize(Constants.POST_IMAGE_RESIZE_VALUE, Constants.POST_IMAGE_RESIZE_VALUE)
                .into(binding.postImage, object : Callback {
                    override fun onSuccess() {
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
    }

    private fun setError(error: String) {
        binding.errorText.text = error
        binding.errorText.visibility = View.VISIBLE
        binding.loadingImageProgress.visibility = View.GONE
        binding.postImage.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}