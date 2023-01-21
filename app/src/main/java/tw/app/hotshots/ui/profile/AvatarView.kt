package tw.app.hotshots.ui.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import tw.app.hotshots.R
import tw.app.hotshots.authentication.model.UserType
import tw.app.hotshots.databinding.ViewUserAvatarBinding
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.util.Constants

class AvatarView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var _binding: ViewUserAvatarBinding? = null
    private val binding get() = _binding!!

    private val DEFAULT_AVATAR = "default"

    init {
        val inflater = LayoutInflater.from(context)
        _binding = ViewUserAvatarBinding.inflate(inflater, this, true)
    }

    private fun setLoading(isOn: Boolean) {
        if (isOn) {
            binding.avatarLoading.visibility = VISIBLE
            binding.avatarImage.visibility = GONE
        } else {
            binding.avatarLoading.visibility = GONE
            binding.avatarImage.visibility = VISIBLE
        }
    }

    fun setAvatarFromUrl(url: String) {

        if (url == DEFAULT_AVATAR) {
            setAvatarDefault()
        } else {
            setLoading(true)

            Picasso
                .get()
                .load(url)
                .into(binding.avatarImage, object : Callback {
                    override fun onSuccess() {
                        setLoading(false)
                    }

                    override fun onError(e: Exception?) {
                        setLoading(false)

                        val error = e?.message
                            ?: ("Unknown Error | AvatarView" +
                                    "\n" +
                                    "AvatarView#setAvatarFromUrl - on line 57")

                        Logger.log(error, LogType.ERROR)
                    }
                })
        }
    }

    private fun setAvatarDefault() {
        setLoading(false)
        binding.avatarImage.setImageResource(R.drawable.ic_person_wb)
    }

    fun setAvatarLevel(type: UserType) {
        if (type == UserType.BANNED) {
            binding.avatarImage.borderColor = Constants.BANNED_USER_BORDER
        } else if (type == UserType.USER) {
            binding.avatarImage.borderColor = Constants.DEFAULT_USER_BORDER(context)
        } else if (type == UserType.PREMIUM) {
            binding.avatarImage.borderColor = Constants.PREMIUM_USER_BORDER(context)
        } else if (type == UserType.MODERATOR) {
            binding.avatarImage.borderColorStart = Constants.MOD_TOP_USER_BORDER(context)
            binding.avatarImage.borderColorEnd = Constants.MOD_BOTTOM_USER_BORDER(context)
            binding.avatarImage.borderColorDirection =
                CircularImageView.GradientDirection.TOP_TO_BOTTOM
        } else if (type == UserType.ADMINISTRATOR) {
            binding.avatarImage.borderColor = Constants.ADMIN_USER_BORDER(context)
        } else {
            Logger.log(
                "Unknown Avatar Level: $type",
                LogType.ERROR
            )
        }
    }
}