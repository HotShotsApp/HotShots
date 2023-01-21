package tw.app.hotshots.ui.link

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.app.hotshots.databinding.DialogEditLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.util.TimeUtil

class EditLinkDialog(
    context: Context,
    private val link: Link,
    private val listener: EditLinkListener
) : BottomSheetDialog(context) {

    private val binding = DialogEditLinkBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        binding.titleEditText.setText(link.title)
        binding.urlEditText.setText(link.url)

        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            link.title = binding.titleEditText.text.toString()
            link.url = binding.urlEditText.text.toString()
            link.lastModifiedAt = TimeUtil.currentTimeToLong()

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