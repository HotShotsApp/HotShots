package tw.app.hotshots.activity.browser

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout

class ChromeClient(
    private val webView: WebView,
    private val window: Window
) : WebChromeClient() {
    private var fullscreen: View? = null

    // This class handles FullScreen Video Players

    override fun onHideCustomView() {
        fullscreen?.visibility = GONE
        webView.visibility = VISIBLE
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        webView.visibility = GONE

        if (fullscreen != null) {
            (window.decorView as FrameLayout).removeView(fullscreen)
        }

        fullscreen = view
        (window.decorView as FrameLayout).addView(fullscreen, FrameLayout.LayoutParams(-1, -1))
        fullscreen?.visibility = VISIBLE
    }
}