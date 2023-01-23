package tw.app.hotshots.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

class CopyUtil {
    fun copy(text: String, context: Context, toastText: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("hotshots", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
        Vibrate.vibrate(context)
    }
}