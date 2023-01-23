package tw.app.hotshots.ui.link

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.app.hotshots.databinding.DialogEditLinkBinding
import tw.app.hotshots.databinding.DialogLinkOptionsBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

class LinkOptionsDialog(
    context: Context,
    private val listener: LinkOptionsListener
) : BottomSheetDialog(context) {

    private val binding = DialogLinkOptionsBinding.inflate(LayoutInflater.from(context))
    private val settings = Settings.getInstance

    init {
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        binding.openInAppButton.setOnClickListener {
            listener.onOpen(OpenIn.INAPP)
            dismiss()
        }

        binding.openInBrowserButton.setOnClickListener {
            listener.onOpen(OpenIn.BROWSER)
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
    }
}

interface LinkOptionsListener {
    fun onOpen(openIn: OpenIn)
}

enum class OpenIn {
    BROWSER,
    INAPP
}