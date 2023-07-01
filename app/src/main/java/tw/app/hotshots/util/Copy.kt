package tw.app.hotshots.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object Copy {
    public fun Text(context: Context, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("HotShot Error", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Skopiowano", Toast.LENGTH_LONG).show()
    }
}