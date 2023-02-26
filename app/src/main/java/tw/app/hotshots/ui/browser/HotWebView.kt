package tw.app.hotshots.ui.browser

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.MenuItem
import android.webkit.WebView
import java.net.URL

class HotWebView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int
) : WebView(context, attributeSet, defStyle) {

    private val SAVE_IMAGE_ID = 10
    private val SAVE_LINK_ID = 11
    private val COPY_LINK_ID = 12

    private var onContextMenuShowListener: OnContextMenuShowListener? = null

    constructor(context: Context) : this(context, null, 0) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    init {

    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu?) {
        //super.onCreateContextMenu(menu)

        val result = hitTestResult

        when (result.type) {
            HitTestResult.SRC_IMAGE_ANCHOR_TYPE or HitTestResult.IMAGE_TYPE or HitTestResult.IMAGE_ANCHOR_TYPE -> {
                // Image
                onContextMenuShowListener?.onShow(MenuAction.IMAGE, result)
            }

            HitTestResult.SRC_ANCHOR_TYPE -> {
                // Link
                onContextMenuShowListener?.onShow(MenuAction.LINK, result)
            }
        }
    }

    fun setOnContextMenuShowListener(listener: OnContextMenuShowListener) {
        onContextMenuShowListener = listener
    }

    interface OnContextMenuShowListener {
        fun onShow(action: MenuAction, result: HitTestResult)
    }

    enum class MenuAction {
        LINK,
        IMAGE
    }
}