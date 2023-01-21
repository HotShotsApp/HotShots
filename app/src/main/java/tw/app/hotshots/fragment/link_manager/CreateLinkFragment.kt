package tw.app.hotshots.fragment.link_manager

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
import com.google.android.material.snackbar.Snackbar
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.adapter.OnSwipedListener
import tw.app.hotshots.adapter.SwipeToDeleteCallback
import tw.app.hotshots.adapter.recyclerview.LinksAdapter
import tw.app.hotshots.databinding.FragmentCreateLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.ui.link.AddLinkDialog
import tw.app.hotshots.ui.link.AddLinkListener
import tw.app.hotshots.ui.link.CreateLinkDialog
import tw.app.hotshots.ui.link.EditLinkListener

class CreateLinkFragment : Fragment() {
    private var _binding: FragmentCreateLinkBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private lateinit var linkManager: LinkManager

    private var createLinkDialog: CreateLinkDialog? = null
    private var createLinkListener: CreateLinkDialog.Companion.CreateLinkDialogListener? = null

    private var _adapter: LinksAdapter? = null
    private val adapter get() = _adapter!!

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
                val position = viewHolder.adapterPosition
                val link = adapter.getLink(position)

                mainActivity.runOnUiThread {
                    Toast.makeText(mainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    linkManager.removeLink(link)
                }
            }
        })
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.linksRecyclerView)
    }
}