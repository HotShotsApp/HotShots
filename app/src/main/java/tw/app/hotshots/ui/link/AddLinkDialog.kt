package tw.app.hotshots.ui.link

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View.GONE
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.app.hotshots.activity.AvatarPickerListener
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.databinding.DialogEditLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.FileUtil
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator
import tw.app.hotshots.util.UriUtil

class AddLinkDialog(
    private val contextActivity: Context,
    private val listener: AddLinkListener
) : BottomSheetDialog(contextActivity) {

    private val binding = DialogEditLinkBinding.inflate(LayoutInflater.from(context))
    private val settings = Settings.getInstance
    private var imageFilePath = ""

    init {
        binding.editLinkTitle.text = "Dodaj Odnośnik"
        setContentView(binding.root)

        setup()
    }

    fun hideImageView() {
        binding.linkImageHolder.visibility = GONE
        binding.removeImageButton.visibility = GONE
    }

    private fun setup() {
        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }

        binding.linkImageHolder.setOnClickListener {
            val mainActivity = (contextActivity as MainActivity)

            mainActivity.shouldUploadToDatabase = false
            mainActivity.setAvatarPickerListener(object : AvatarPickerListener {
                override fun onCropped(croppedBitmap: Bitmap) {
                    super.onCropped(croppedBitmap)
                    binding.linkImage.setImageBitmap(croppedBitmap)
                    var file = FileUtil.saveBitmapAsFile(context, croppedBitmap)
                    imageFilePath = Uri.fromFile(file).toString()
                }
            })

            mainActivity.pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.removeImageButton.setOnClickListener {
            imageFilePath = ""
            binding.linkImage.setImageDrawable(null)
        }

        binding.saveButton.setOnClickListener {
            val link = Link(
                id = UidGenerator.Generate(12),
                url = binding.urlEditText.text.toString(),
                title = binding.titleEditText.text.toString(),
                createdAt = TimeUtil.currentTimeToLong(),
                lastModifiedAt = TimeUtil.currentTimeToLong()
            )

            if (imageFilePath.isNotBlank())
                link.imageUrl = imageFilePath

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