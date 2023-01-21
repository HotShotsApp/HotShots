package tw.app.hotshots.ui.settings

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.app.hotshots.databinding.DialogPinSetupBinding
import tw.app.hotshots.extensions.showKeyboard

class PinSetupDialog(
    context: Context,
    private val listener: PinSetupListener
) : BottomSheetDialog(context) {

    private val binding = DialogPinSetupBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        binding.closeDialogButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            listener.onSaved(binding.otpView.otp!!)
            dismiss()
        }

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                if (binding.otpView.otp!!.length <= 5) {
                    binding.saveButton.isEnabled = false
                }
            }

            override fun onOTPComplete(otp: String) {
                binding.saveButton.isEnabled = true
            }
        }
    }

    override fun dismiss() {
        binding.otpView.setOTP("")
        super.dismiss()
    }

    override fun show() {
        super.show()

        binding.otpView.showKeyboard()
    }
}

interface PinSetupListener {
    fun onSaved(otp: String)
}