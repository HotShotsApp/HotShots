package tw.app.hotshots.fragment.link_manager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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

class CreateLinkFragment : Fragment() {
    private var _binding: FragmentCreateLinkBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private var settings = Settings.getInstance
    private lateinit var linkManager: LinkManager

    private var createLinkDialog: CreateLinkDialog? = null
    private var createLinkListener: CreateLinkDialog.Companion.CreateLinkDialogListener? = null

    private var _adapter: LinksAdapter? = null
    private val adapter get() = _adapter!!
    private var prevClickedLink: Link? = null

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
                    adapter.remove(position)

                    Snackbar.make(binding.root, "Odnośnik został usunięty", Snackbar.LENGTH_LONG)
                        .setAction("Przywróć") { linkManager.addLink(link)}.show()
                }

                super.onRemoved(link, position)
            }

            override fun onSaved() {
                super.onSaved()
            }
        })

        val editLinkListener = object : EditLinkListener {
            override fun onEdited(link: Link) {
                linkManager.modifyLink(link)
            }
        }

        _adapter = LinksAdapter(linkManager.getLinks(), mainActivity, editLinkListener)
        val layoutManager = LinearLayoutManager(mainActivity)
        binding.linksRecyclerView.layoutManager = layoutManager
        binding.linksRecyclerView.adapter = adapter

        var linkOptionsDialog = LinkOptionsDialog(requireContext(), object : LinkOptionsListener {
            override fun onOpen(openIn: OpenIn) {
                when(openIn) {
                    OpenIn.BROWSER -> {
                        val data = Uri.parse(prevClickedLink?.url!!)
                        val defaultBrowser: Intent =
                            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                        defaultBrowser.setData(data)
                        startActivity(defaultBrowser)
                    }

                    OpenIn.INAPP -> {
                        if (!settings.isWarningAboutInAppBrowserReaded()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Uwaga!")
                                .setMessage("Aplikacja jest nadal w wersji ALPHA.\nPrzeglądarka wbudowana w aplikacje nie zapewnia bezpieczeństwa w sieci.")
                                .setPositiveButton("Okej") {_, _ ->
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
                }
            }
        })

        adapter.setOnClickListener(object : LinkClickListener {
            override fun onClicked(link: Link) {
                prevClickedLink = link
                linkOptionsDialog.show()
            }
        })

        if (linkManager.getLinks().isNotEmpty()) {
            binding.noLinksFindedTextView.visibility = GONE
        }

        val addLinkDialog = AddLinkDialog(mainActivity, object : AddLinkListener {
            override fun onAdded(link: Link) {
                linkManager.addLink(link)
            }
        })

        binding.createLinkButton.setOnClickListener {
            addLinkDialog.show()
        }

        val swipeToDeleteCallback = SwipeToDeleteCallback(mainActivity, object : OnSwipedListener {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val link = adapter.getLink(position)

                mainActivity.runOnUiThread {
                    linkManager.removeLink(link)
                }
            }
        })
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.linksRecyclerView)
    }

    override fun onResume() {
        adapter.setList(linkManager.getLinks())

        super.onResume()
    }

    private fun openInAppBrowser() {
        val intent = Intent(activity, BrowserActivity::class.java)
        intent.putExtra("url", prevClickedLink?.url!!)
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}