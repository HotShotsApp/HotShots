package tw.app.hotshots.activity.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import tw.app.hotshots.R
import tw.app.hotshots.databinding.ActivityBrowserBinding
import tw.app.hotshots.fragment.link_manager.LinkManager
import tw.app.hotshots.fragment.link_manager.LinkManagerListener
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.ui.link.AddLinkDialog
import tw.app.hotshots.ui.link.AddLinkListener

class BrowserActivity : AppCompatActivity() {
    private var _binding: ActivityBrowserBinding? = null
    private val binding get() = _binding!!

    private lateinit var popup: PopupMenu
    private lateinit var saveLinkDialog: AddLinkDialog
    private lateinit var linkManager: LinkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buildSaveLinkDialog()
        buildPopupMenu()

        binding.browserWebview.settings.javaScriptEnabled = true

        val webChromeClient = ChromeClient(
            binding.browserWebview,
            window
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
                    binding.backPageButton.isEnabled = true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressView.visibility = INVISIBLE

                binding.urlTextView.setText(url!!)

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

        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.browserSwipe.setOnRefreshListener {
            binding.browserWebview.reload()
        }
    }

    private fun buildSaveLinkDialog() {
        linkManager = LinkManager(this@BrowserActivity, object : LinkManagerListener {
            override fun onAdded(link: Link, position: Int) {
                Toast.makeText(this@BrowserActivity, "Zapisano!", Toast.LENGTH_SHORT).show()
            }
        })

        // TODO: FIX this so when new instance of LinkManager created, LinkManager automatically gets an saved links
        linkManager.getLinks()

        saveLinkDialog = AddLinkDialog(this@BrowserActivity, object : AddLinkListener {
            override fun onAdded(link: Link) {
                linkManager.addLink(link)
            }
        })
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
            }

            true
        }

        binding.moreButton.setOnClickListener {
            popup.show()
        }
    }
}