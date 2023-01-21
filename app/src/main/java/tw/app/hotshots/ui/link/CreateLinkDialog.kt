package tw.app.hotshots.ui.link

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tw.app.hotshots.databinding.DialogAddLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator

class CreateLinkDialog(
    private val activityContext: Context,
    private val listener: CreateLinkDialogListener
) : MaterialAlertDialogBuilder(activityContext) {

    private var _binding: DialogAddLinkBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = DialogAddLinkBinding.inflate(LayoutInflater.from(activityContext))
        setView(binding.root)

        setTitle("Dodaj Odnośnik")
        setMessage("Odnośniki są zapisywane na urządzeniu.")

        setPositiveButton(
            "Dodaj"
        ) { _, _ ->
            val link = Link(
                id = UidGenerator.Generate(12),
                url = binding.urlEditText.text.toString(),
                title = binding.titleEditText.text.toString(),
                createdAt = TimeUtil.currentTimeToLong(),
                lastModifiedAt = TimeUtil.currentTimeToLong()
            )

            listener.onAddClicked(link)
        }

        setNegativeButton(
            "Anuluj"
        ) { _, _ -> listener.onDismissClicked() }
    }

    companion object {
        interface CreateLinkDialogListener {
            fun onAddClicked(link: Link)

            fun onDismissClicked()
        }
    }
}