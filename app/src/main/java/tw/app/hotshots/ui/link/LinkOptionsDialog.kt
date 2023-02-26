package tw.app.hotshots.ui.link

import android.content.Context
import android.content.res.Resources.Theme
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import tw.app.hotshots.R
import tw.app.hotshots.databinding.DialogEditLinkBinding
import tw.app.hotshots.databinding.DialogLinkOptionsBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.CopyUtil
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

class LinkOptionsDialog(
    context: Context,
    private val listener: LinkOptionsListener
) : BottomSheetDialog(context) {

    private val binding = DialogLinkOptionsBinding.inflate(LayoutInflater.from(context))
    private val settings = Settings.getInstance

    private var position: Int = 0

    init {
        setContentView(binding.root)
        setup()
    }

    private fun setup() {
        binding.openInAppButton.setOnClickListener {
            listener.onOpen(OpenIn.INAPP, position)
            dismiss()
        }

        binding.openInBrowserButton.setOnClickListener {
            listener.onOpen(OpenIn.BROWSER, position)
            dismiss()
        }

        binding.copyLinkButton.setOnClickListener {
            listener.onOpen(OpenIn.COPY, position)
            dismiss()
        }

        binding.removeLinkButton.setOnClickListener {
            listener.onOpen(OpenIn.DELETE, position)
            dismiss()
        }

    }

    fun setCurrentLink(link: Link, pos: Int) {
        position = pos

        if (link.imageUrl.isNotBlank()) {
            setImage(link.imageUrl)
        } else {
            binding.linkImageView.visibility = GONE
            binding.linkImageViewShadow.visibility = GONE
        }

        binding.linkTitleText.text = link.title
        binding.createdAtText.text = TimeUtil.convertLongToTime(link.createdAt)
    }

    private fun setImage(linkImagePath: String) {
        binding.linkImageView.visibility = VISIBLE
        binding.linkImageViewShadow.visibility = VISIBLE

        if (settings.isPrivateLinkViewEnabled()) {
            Picasso.get().load(R.drawable.private_image).into(binding.linkImageView)
        } else {
            Picasso.get().load(Uri.parse(linkImagePath)).into(binding.linkImageView)
        }
    }

    override fun dismiss() {
        super.dismiss()
    }
}

interface LinkOptionsListener {
    fun onOpen(openIn: OpenIn, position: Int)
}

enum class OpenIn {
    BROWSER,
    INAPP,
    COPY,
    DELETE
}