package tw.app.hotshots.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import tw.app.hotshots.databinding.ItemLinkBinding
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.ui.link.EditLinkDialog
import tw.app.hotshots.ui.link.EditLinkListener

class LinksAdapter(
    private var mData: MutableList<Link>,
    private var context: Context,
    private var editLinkListener: EditLinkListener // To Listen for changes in CreateLinkFragment
) : RecyclerView.Adapter<LinksAdapter.ViewHolder>() {
    private var listener: LinkClickListener? = null
    inner class ViewHolder(val binding: ItemLinkBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinksAdapter.ViewHolder {
        val binding = ItemLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val link = mData[position]

        if (link.imageUrl.isNotBlank()) {
            binding.linkImageHolder.visibility = VISIBLE

            Picasso.get()
                .load(link.imageUrl)
                .into(binding.linkImage)
        }

        binding.linkTitleText.text = link.title
        binding.linkUrlText.text = link.url

        binding.editButton.setOnClickListener {
            var editLinkDialog = EditLinkDialog(context, link, editLinkListener)
            editLinkDialog.show()
        }

        binding.root.setOnClickListener {
            if (listener != null) {
                listener?.onClicked(link)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun add(link: Link) {
        mData.add(0, link)
        notifyItemInserted(0)
    }

    fun remove(position: Int) {
        mData.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeAll() {
        mData = arrayListOf()
        notifyDataSetChanged()
    }

    fun replace(link: Link, position: Int) {
        mData[position] = link
        notifyItemChanged(position)
    }

    fun getLink(position: Int): Link {
        return mData[position]
    }

    fun setList(list: MutableList<Link>) {
        mData = list
        notifyDataSetChanged()
    }

    fun setOnClickListener(_listener: LinkClickListener) {
        listener = _listener
    }
}

interface LinkClickListener {
    fun onClicked(link: Link)
}