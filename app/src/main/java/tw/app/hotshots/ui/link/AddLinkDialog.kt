package tw.app.hotshots.ui.link

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.app.hotshots.databinding.DialogEditLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

class AddLinkDialog(
    context: Context,
    private val listener: AddLinkListener
) : BottomSheetDialog(context) {

    private val binding = DialogEditLinkBinding.inflate(LayoutInflater.from(context))
    private val settings = Settings.getInstance

    init {
        binding.editLinkTitle.text = "Dodaj Odnośnik"
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            val link = Link(
                id = UidGenerator.Generate(12),
                url = binding.urlEditText.text.toString(),
                title = binding.titleEditText.text.toString(),
                createdAt = TimeUtil.currentTimeToLong(),
                lastModifiedAt = TimeUtil.currentTimeToLong()
            )

            listener.onAdded(link)
            dismiss()
        }

        enableTitleAutoComplete()
    }

    private fun enableTitleAutoComplete() {
        if (settings.isLinkTitleAutoCompleteEnabled()) {
            val webView = WebView(context)

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                        webView.loadUrl(s.toString())
                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)

                                var pageTitle = view!!.title ?: ""
                                if (pageTitle.isNotBlank() && pageTitle.length > 1 && pageTitle.contains("-")) {
                                    val charPosition = pageTitle.lastIndexOf("-")
                                    pageTitle = pageTitle.substring(0, charPosition).trim()
                                    binding.titleEditText.setText(pageTitle)
                                }
                            }
                        }
                }
            }

            binding.urlEditText.addTextChangedListener(textWatcher)
        }
    }

    fun setUrl(url: String) {
        binding.urlEditText.setText(url)
    }

    fun setTitle(title: String) {
        binding.titleEditText.setText(title)
    }

    override fun dismiss() {
        binding.urlEditText.setText("")
        binding.titleEditText.setText("")
        super.dismiss()
    }
}

interface AddLinkListener {
    fun onAdded(link: Link)
}