package tw.app.hotshots.fragment.link_manager

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.activity.browser.BrowserActivity
import tw.app.hotshots.adapter.OnSwipedListener
import tw.app.hotshots.adapter.SwipeToDeleteCallback
import tw.app.hotshots.adapter.recyclerview.LinkClickListener
import tw.app.hotshots.adapter.recyclerview.LinksAdapter
import tw.app.hotshots.databinding.FragmentCreateLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.ui.link.*
import tw.app.hotshots.util.CopyUtil
import tw.app.hotshots.util.FileUtil
import kotlin.coroutines.CoroutineContext

class CreateLinkFragment : Fragment(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: FragmentCreateLinkBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private var settings = Settings.getInstance
    private lateinit var linkManager: LinkManager

    private var createLinkDialog: CreateLinkDialog? = null
    private var createLinkListener: CreateLinkDialog.Companion.CreateLinkDialogListener? = null

    private var _linkOptionsDialog: LinkOptionsDialog? = null
    private val linkOptionsDialog get() = _linkOptionsDialog!!

    private var _adapter: LinksAdapter? = null
    private val adapter get() = _adapter!!
    private var prevClickedLink: Link? = null

    private var links: MutableList<Link> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateLinkBinding.inflate(inflater, container, false)
        mainActivity = (activity as MainActivity)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.goUpButton.setOnClickListener {
            binding.linksRecyclerView.smoothScrollToPosition(0)
        }

        linkManager = LinkManager(mainActivity, object : LinkManagerListener {
            override fun onAdded(link: Link, position: Int) {
                mainActivity.runOnUiThread {
                    adapter.add(link)
                }

                super.onAdded(link, position)
            }

            override fun onModified(link: Link, position: Int) {
                mainActivity.runOnUiThread {
                    adapter.replace(link, position)
                }

                super.onModified(link, position)
            }

            override fun onRemoved(link: Link, position: Int) {
                mainActivity.runOnUiThread {
                    Snackbar.make(binding.root, "Odnośnik został usunięty", Snackbar.LENGTH_LONG)
                        .setAction("Przywróć") { linkManager.addLink(link) }.show()
                }

                super.onRemoved(link, position)
            }

            override fun onSaved() {
                super.onSaved()
            }
        })

        getLinksAndSetup()

        _linkOptionsDialog = LinkOptionsDialog(requireContext(), object : LinkOptionsListener {
            override fun onOpen(openIn: OpenIn, position: Int) {
                when (openIn) {
                    OpenIn.BROWSER -> {
                        val data = Uri.parse(prevClickedLink?.url!!)
                        val defaultBrowser: Intent =
                            Intent.makeMainSelectorActivity(
                                Intent.ACTION_MAIN,
                                Intent.CATEGORY_APP_BROWSER
                            )
                        defaultBrowser.setData(data)
                        startActivity(defaultBrowser)
                    }

                    OpenIn.INAPP -> {
                        if (!settings.isWarningAboutInAppBrowserReaded()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Uwaga!")
                                .setMessage("Aplikacja jest nadal w wersji ALPHA.\nPrzeglądarka wbudowana w aplikacje nie zapewnia bezpieczeństwa w sieci.")
                                .setPositiveButton("Okej") { _, _ ->
                                    openInAppBrowser()
                                }
                                .show()
                        } else {
                            openInAppBrowser()
                        }
                    }

                    OpenIn.COPY -> {
                        CopyUtil().copy(
                            prevClickedLink!!.url,
                            requireContext(),
                            "Skopiowano URL!"
                        )
                    }

                    OpenIn.DELETE -> {
                        adapter.remove(position)
                        linkManager.removeLink(prevClickedLink!!)
                    }
                }
            }
        })

        binding.noLinksFindedTextView.visibility = GONE

        val addLinkDialog = AddLinkDialog(mainActivity, object : AddLinkListener {
            override fun onAdded(link: Link) {
                linkManager.addLink(link)
            }
        })

        binding.createLinkButton.setOnClickListener {
            addLinkDialog.show()
        }

        binding.openBrowserButton.setOnClickListener {
            if (!settings.isWarningAboutInAppBrowserReaded()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Uwaga!")
                    .setMessage("Aplikacja jest nadal w wersji ALPHA.\nPrzeglądarka wbudowana w aplikacje nie zapewnia bezpieczeństwa w sieci.")
                    .setPositiveButton("Okej") { _, _ ->
                        openInAppBrowser()
                    }
                    .show()
            } else {
                openInAppBrowser(settings.getDefaultWebSite())
            }
        }

        binding.settingsButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ustawienia")
                .setItems(
                    arrayOf("Usuń odnośniki", "Utwórz kopię", "Przywróć z kopii", "Usuń kopię")
                ) { _, which ->
                    if (which == 0) {
                        linkManager.removeAll()
                        adapter.removeAll()
                    } else if (which == 1) {
                        linkManager.backupWrite(object : FileUtil.OnWriteToFileListener {
                            override fun onSuccess() {
                                Toast.makeText(
                                    requireContext(),
                                    "Zapisano kopię!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            override fun onError(reason: String) {
                                Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show()
                            }
                        })
                    } else if (which == 2) {
                        if (linkManager.isBackupAvailable()) {
                            linkManager.backupRestore()
                        } else {
                            Toast.makeText(requireContext(), "Brak kopii!", Toast.LENGTH_LONG).show()
                        }
                    } else if (which == 3) {
                        if (linkManager.isBackupAvailable()) {
                            linkManager.removeBackup()
                        } else {
                            Toast.makeText(requireContext(), "Brak kopii!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .show()
        }

        val swipeToDeleteCallback = SwipeToDeleteCallback(mainActivity, object : OnSwipedListener {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                mainActivity.runOnUiThread {
                    val position = viewHolder.bindingAdapterPosition
                    val link = adapter.getLink(position)
                    linkManager.removeLink(link)
                }
            }
        })
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.linksRecyclerView)
    }

    override fun onResume() {
        getLinksAndSetToAdapter()

        super.onResume()
    }

    private fun getLinksAndSetup() {
        launch {
            linkManager.getLinks(object : GetLinksListener {
                override fun onReceived(_links: MutableList<Link>) {
                    requireActivity().runOnUiThread {
                        links = _links

                        val editLinkListener = object : EditLinkListener {
                            override fun onEdited(link: Link) {
                                linkManager.modifyLink(link)
                            }
                        }

                        _adapter = LinksAdapter(links, mainActivity, editLinkListener)
                        val layoutManager = LinearLayoutManager(mainActivity)
                        binding.linksRecyclerView.layoutManager = layoutManager
                        binding.linksRecyclerView.adapter = adapter

                        /*binding.linksRecyclerView.addOnScrollListener(object :
                            RecyclerView.OnScrollListener() {
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                //super.onScrolled(recyclerView, dx, dy)

                                if (dy < 0) {
                                    // Scrolling Up

                                    if (layoutManager.findFirstCompletelyVisibleItemPosition() <= 8) {
                                        if (binding.goUpButton.isOrWillBeShown) {
                                            binding.goUpButton.hide()
                                        }
                                    }

                                    if (binding.settingsButton.isOrWillBeHidden) {
                                        binding.settingsButton.show()
                                        binding.openBrowserButton.show()
                                        binding.createLinkButton.show()
                                    }
                                } else if (dy > 0) {
                                    // Scrolling Down

                                    if (layoutManager.findFirstCompletelyVisibleItemPosition() >= 8) {
                                        if (binding.goUpButton.isOrWillBeHidden) {
                                            binding.goUpButton.show()
                                        }
                                    }

                                    if (binding.settingsButton.isOrWillBeShown) {
                                        binding.settingsButton.hide()
                                        binding.openBrowserButton.hide()
                                        binding.createLinkButton.hide()
                                    }
                                }
                            }
                        })*/

                        adapter.setOnClickListener(object : LinkClickListener {
                            override fun onClicked(link: Link, position: Int) {
                                prevClickedLink = link
                                Log.d("LinkData", "onClicked: Link => $link")
                                linkOptionsDialog.setCurrentLink(link, position)
                                linkOptionsDialog.show()
                            }
                        })
                    }
                }
            })
        }
    }

    private fun getLinksAndSetToAdapter() {
        launch {
            linkManager.getLinks(object : GetLinksListener {
                override fun onReceived(_links: MutableList<Link>) {
                    requireActivity().runOnUiThread {
                        links = _links
                        adapter.setList(links)
                    }
                }
            })
        }
    }

    private fun openInAppBrowser() {
        val intent = Intent(activity, BrowserActivity::class.java)
        intent.putExtra("url", prevClickedLink?.url!!)
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun openInAppBrowser(url: String) {
        val intent = Intent(activity, BrowserActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}