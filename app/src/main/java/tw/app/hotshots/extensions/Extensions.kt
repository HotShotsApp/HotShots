package tw.app.hotshots.extensions

import `in`.aabhasjindal.otptextview.OtpTextView
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import tw.app.hotshots.R
import tw.app.hotshots.ui.imageview.TopCropImageView


class Extensions {

}

/**
 * ImageView's
 */
fun ImageView.setImageBlackWhite() {
    colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
}

fun TopCropImageView.setImageBlackWhite(colorOverlay: Int?) {
    colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
        //setSaturation(0f);
        if (colorOverlay != null) {
            setColorFilter(colorOverlay, PorterDuff.Mode.DARKEN)
        }
    })
}

fun TopCropImageView.setColorLayer() {

}

/**
 * Input View's
 */
fun OtpTextView.hideKeyboard() {
    clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(getWindowToken(), 0)
}

fun OtpTextView.showKeyboard() {
    requestFocusOTP()
    /*val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)*/
}

fun EditText.hideKeyboard() {
    clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(getWindowToken(), 0)
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}