package tw.app.hotshots.ui.dialog.loading

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import tw.app.hotshots.R
import tw.app.hotshots.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) {
    lateinit var binding: DialogLoadingBinding
    lateinit var dialog: AlertDialog
    var mContext = context

    fun build() {
        binding = DialogLoadingBinding.inflate(LayoutInflater.from(mContext))

        val dialogBuilder = AlertDialog.Builder(mContext)

        dialogBuilder.setView(binding.root)
        dialogBuilder.setCancelable(false)

        dialog = dialogBuilder.create()
    }

    fun setTitle(title: String) {
        binding.titleText.text = title
    }

    fun setMessage(message: String) {
        binding.messageText.text = message
    }

    fun setIndeterminate(indeterminate: Boolean) {
        binding.loadingBar.isIndeterminate = indeterminate
    }

    fun setProgress(progress: Int) {
        if (binding.loadingBar.isIndeterminate)
            binding.loadingBar.isIndeterminate = false

        binding.loadingBar.progress = progress
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
            dialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    mContext,
                    R.drawable.transparent
                )
            )
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun toggleDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        } else {
            dialog.show()
        }
    }
}