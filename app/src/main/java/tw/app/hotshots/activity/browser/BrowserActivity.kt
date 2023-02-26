package tw.app.hotshots.activity.browser

import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.R
import tw.app.hotshots.activity.browser.client.ChromeClient
import tw.app.hotshots.databinding.ActivityBrowserBinding
import tw.app.hotshots.fragment.link_manager.GetLinksListener
import tw.app.hotshots.fragment.link_manager.LinkManager
import tw.app.hotshots.fragment.link_manager.LinkManagerListener
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.ui.browser.HotWebView
import tw.app.hotshots.ui.link.AddLinkDialog
import tw.app.hotshots.ui.link.AddLinkListener
import tw.app.hotshots.util.CopyUtil
import java.net.URL
import kotlin.coroutines.CoroutineContext

class BrowserActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: ActivityBrowserBinding? = null
    private val binding get() = _binding!!

    private lateinit var popup: PopupMenu
    private lateinit var saveLinkDialog: AddLinkDialog
    private lateinit var linkManager: LinkManager

    private lateinit var imageContextMenu: MaterialAlertDialogBuilder
    private lateinit var linkContextMenu: MaterialAlertDialogBuilder

    private var canGoBack = false

    private var longClickUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buildSaveLinkDialog()
        buildPopupMenu()
        buildImageContextMenu()
        buildLinkContextMenu()

        binding.browserWebview.settings.javaScriptEnabled = true

        val webChromeClient = ChromeClient(
            binding.browserWebview,
            window,
            object : ChromeClient.OnLoadingProgressChanged {
                override fun onProgress(progress: Int) {
                    if (progress >= 99) {
                        if (!binding.progressView.isIndeterminate) {
                            binding.progressView.isIndeterminate = true
                        }

                        return
                    }

                    binding.progressView.isIndeterminate = false
                    binding.progressView.progress = progress
                }
            }
        )

        binding.browserWebview.webChromeClient = webChromeClient

        binding.browserWebview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                binding.progressView.visibility = VISIBLE

                if (favicon != null)
                    binding.favIconImage.setImageBitmap(favicon)

                binding.urlTextView.setText(url!!)

                if (binding.browserWebview.canGoBack()) {
                    canGoBack = true
                    binding.backPageButton.isEnabled = true
                } else {
                    canGoBack = false
                    binding.backPageButton.isEnabled = false
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressView.visibility = GONE

                binding.urlTextView.setText(url!!)

                if (binding.browserWebview.canGoBack()) {
                    canGoBack = true
                    binding.backPageButton.isEnabled = true
                } else {
                    canGoBack = false
                    binding.backPageButton.isEnabled = false
                }

                if (binding.browserSwipe.isRefreshing)
                    binding.browserSwipe.isRefreshing = false
            }
        }

        binding.urlTextView.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.browserWebview.loadUrl(binding.urlTextView.text.toString())
                true
            }

            false
        }

        val initUrl = intent.getStringExtra("url")!!

        binding.browserWebview.loadUrl(initUrl)

        binding.backPageButton.setOnClickListener {
            binding.browserWebview.goBack()
        }

        binding.browserSwipe.setOnRefreshListener {
            binding.browserWebview.reload()
        }

        binding.browserWebview.setOnContextMenuShowListener(object : HotWebView.OnContextMenuShowListener {
            override fun onShow(action: HotWebView.MenuAction, result: WebView.HitTestResult) {

                longClickUrl = result.extra.toString()

                when (action) {
                    HotWebView.MenuAction.LINK -> {
                        linkContextMenu.setTitle(formatNameFromHostName(URL(longClickUrl).host))
                        linkContextMenu.show()
                    }

                    HotWebView.MenuAction.IMAGE -> {
                        imageContextMenu.setTitle(formatNameFromHostName(URL(longClickUrl).host))
                        imageContextMenu.show()
                    }
                }
            }
        })
    }

    private fun formatNameFromHostName(hostName: String): String {
        var finalLink = hostName

        if (finalLink.contains("www."))
            finalLink = finalLink.replace("www.", "")

        if (finalLink.contains("/"))
            finalLink = finalLink.replace("/", "")

        finalLink = finalLink.substring(0, finalLink.lastIndexOf("."))

        var firstChar = finalLink.substring(0, 1)
        firstChar = firstChar.uppercase()

        finalLink = firstChar + finalLink.substring(1, finalLink.length)

        return finalLink
    }

    override fun onBackPressed() {
        if (canGoBack) {
            binding.browserWebview.goBack()
        } else {
            finish()
        }
    }

    private fun buildImageContextMenu() {
        imageContextMenu = MaterialAlertDialogBuilder(this@BrowserActivity)
            .setItems(
                arrayOf("Pobierz", "Skopiuj odnośnik")
            ) { dialog, which ->
                if (which == 0) {
                    Toast.makeText(this@BrowserActivity, "Pobierz", Toast.LENGTH_LONG).show()
                } else {
                    CopyUtil()
                        .copy(
                            longClickUrl,
                            this@BrowserActivity,
                            "Skopiowano URL!"
                        )
                }
            }
    }

    private fun buildLinkContextMenu() {
        linkContextMenu = MaterialAlertDialogBuilder(this@BrowserActivity)
            .setItems(
                arrayOf("Zapisz odnośnik", "Skopiuj odnośnik")
            ) { dialog, which ->
                if (which == 0) {
                    saveLinkDialog.show()
                    saveLinkDialog.setUrl(longClickUrl)
                } else {
                    CopyUtil()
                        .copy(
                            longClickUrl,
                            this@BrowserActivity,
                            "Skopiowano URL!"
                        )
                }
            }
    }

    private fun buildSaveLinkDialog() {
        linkManager = LinkManager(this@BrowserActivity, object : LinkManagerListener {
            override fun onAdded(link: Link, position: Int) {
                Toast.makeText(this@BrowserActivity, "Zapisano!", Toast.LENGTH_SHORT).show()
            }
        })

        // TODO: FIX this so when new instance of LinkManager created, LinkManager automatically gets an saved links
        launch {
            linkManager.getLinks(object : GetLinksListener {
                override fun onReceived(links: MutableList<Link>) {

                }
            })
        }

        saveLinkDialog = AddLinkDialog(this@BrowserActivity, object : AddLinkListener {
            override fun onAdded(link: Link) {
                linkManager.addLink(link)
            }
        })

        /**
         * User can't pick cover image
         * because picker is in MainActivity
         * which is not available in [BrowserActivity]!
         *
         * TODO: Find a way to extract images from website /or/ pick image from gallery
         */

        saveLinkDialog.hideImageView()
    }

    private fun buildPopupMenu() {
        popup = PopupMenu(this@BrowserActivity, binding.moreButton)
        popup.inflate(R.menu.menu_browser)

        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.save_to_links_button -> {
                    saveLinkDialog.show()
                    saveLinkDialog.setUrl(binding.browserWebview.url ?: "Nie można przechwycić odnośnika!")
                    saveLinkDialog.setTitle(binding.browserWebview.title ?: "")
                }

                R.id.exit_button -> {
                    finish()
                }
            }

            true
        }

        binding.moreButton.setOnClickListener {
            popup.show()
        }
    }
}