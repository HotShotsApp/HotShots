package tw.app.hotshots.ui.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.app.hotshots.databinding.DialogChangePasswordBinding
import tw.app.hotshots.extensions.showKeyboard

class ChangePasswordDialog (
    context: Context,
    private val listener: PasswordChangeListener
) : BottomSheetDialog(context) {

    private val binding = DialogChangePasswordBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            listener.onChanged(binding.newPasswordEditText.toString())

            dismiss()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    binding.saveButton.isEnabled = false
                } else {
                    validate()
                }
            }
        }

        binding.currentPasswordEditText.addTextChangedListener(watcher)
        binding.newPasswordEditText.addTextChangedListener(watcher)
        binding.confirmNewPasswordEditText.addTextChangedListener(watcher)
    }

    private fun validate() {
        binding.saveButton.isEnabled = binding.newPasswordEditText.text.toString() == binding.confirmNewPasswordEditText.text.toString()
    }

    override fun dismiss() {
        binding.currentPasswordEditText.setText("")
        binding.newPasswordEditText.setText("")
        binding.confirmNewPasswordEditText.setText("")
        super.dismiss()
    }

    override fun show() {
        super.show()

        binding.currentPasswordEditText.showKeyboard()
    }
}

interface PasswordChangeListener {
    fun onChanged(password: String)
}