package tw.app.hotshots.adapter.viewpager.post.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import tw.app.hotshots.databinding.FragmentPostImageBinding
import tw.app.hotshots.util.Constants

/**
 * Fragment that displays post image
 * @param imageUrl Image URL to be shown
 */
class PostImageFragment(
    private val imageUrl: String
) : Fragment() {
    private var _binding: FragmentPostImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso
            .get()
            .load(imageUrl)
            .centerCrop()
            .resize(Constants.POST_IMAGE_RESIZE_VALUE, Constants.POST_IMAGE_RESIZE_VALUE)
            .into(binding.postImage, object : Callback {
                override fun onSuccess() {
                    binding.postImage.visibility = VISIBLE
                    binding.loadingImageProgress.visibility = GONE
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
        binding.errorText.visibility = VISIBLE
        binding.loadingImageProgress.visibility = GONE
        binding.postImage.visibility = GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}