package tw.app.hotshots.ui.link

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import tw.app.hotshots.activity.AvatarPickerListener
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.databinding.DialogEditLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.util.FileUtil
import tw.app.hotshots.util.TimeUtil

class EditLinkDialog(
    private val contextActivity: Context,
    private val link: Link,
    private val listener: EditLinkListener
) : BottomSheetDialog(contextActivity) {

    private val binding = DialogEditLinkBinding.inflate(LayoutInflater.from(context))

    private var imageFilePath = ""

    init {
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        imageFilePath = link.imageUrl

        binding.titleEditText.setText(link.title)
        binding.urlEditText.setText(link.url)

        Picasso
            .get()
            .load(Uri.parse(link.imageUrl))
            .into(binding.linkImage)

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

        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }

        binding.removeImageButton.setOnClickListener {
            link.imageUrl = ""
            imageFilePath = ""
            binding.linkImage.setImageDrawable(null)
        }

        binding.saveButton.setOnClickListener {
            link.title = binding.titleEditText.text.toString()
            link.url = binding.urlEditText.text.toString()
            link.lastModifiedAt = TimeUtil.currentTimeToLong()
            link.imageUrl = imageFilePath

            listener.onEdited(link)

            dismiss()
        }
    }

    override fun dismiss() {
        binding.urlEditText.setText("")
        binding.titleEditText.setText("")
        super.dismiss()
    }
}

interface EditLinkListener {
    fun onEdited(link: Link)
}