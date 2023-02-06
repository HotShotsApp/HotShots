package tw.app.hotshots.adapter.recyclerview

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import tw.app.hotshots.databinding.ItemMediaPickerImageBinding
import tw.app.hotshots.databinding.ItemMediaPickerPickBinding
import java.io.File

class MediaPickerAdapter(
    private val mImages: MutableList<HashMap<String, Any?>>,
    private val listener: MediaPickerListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_PICKER = 0
    private val VIEW_TYPE_IMAGE = 1

    private val MAX_IMAGE_LIMIT = 20

    private var onStartDragListener: OnStartDragListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_PICKER)
            PickerViewHolder.from(parent)
        else
            ImageViewHolder.from(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mImages.size) VIEW_TYPE_PICKER else VIEW_TYPE_IMAGE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PickerViewHolder -> holder.bind(position, listener)
            is ImageViewHolder -> holder.bind(position, mImages, listener, onStartDragListener, holder)
        }
    }

    override fun getItemCount(): Int {
        return mImages.size.coerceAtMost(MAX_IMAGE_LIMIT) + 1;
    }

    fun setOnStartDragListener(listener: OnStartDragListener) {
        onStartDragListener = listener
    }

    fun addImage(map: HashMap<String, Any?>) {
        if (map["imagePath"].toString().isNotBlank()) {
            mImages.add(map)
            notifyItemInserted(mImages.size - 1)
        }
    }

    fun replaceImage(map: HashMap<String, Any?>, position: Int) {
        if (map["imagePath"].toString().isNotBlank()) {
            mImages.set(position, map)
            notifyItemChanged(position)
        }
    }

    fun getPaths(): MutableList<String> {
        val paths: MutableList<String> = arrayListOf()

        for (path in mImages) {
            paths.add(path["imagePath"].toString())
        }

        return paths
    }
}

class PickerViewHolder private constructor(val binding: ItemMediaPickerPickBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int, listener: MediaPickerListener) {
        with(binding.root) {
            this.setOnClickListener {
                listener.onPickClicked(position)
            }
        }
    }

    companion object {
        fun from(parent: ViewGroup): PickerViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemMediaPickerPickBinding.inflate(layoutInflater, parent, false)
            return PickerViewHolder(binding)
        }
    }
}

class ImageViewHolder private constructor(val binding: ItemMediaPickerImageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int, mImages: MutableList<HashMap<String, Any?>>, listener: MediaPickerListener, onStartDragListener: OnStartDragListener?, holder: RecyclerView.ViewHolder) {
        with(binding.root) {
            val image = File(mImages[position]["imagePath"].toString())

            if (onStartDragListener != null) {
                binding.pickedImage.setOnTouchListener(object : View.OnTouchListener {
                    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                        onStartDragListener.onStartDrag(holder)
                        return false
                    }
                })
            }

            Picasso
                .get()
                .load(image)
                .centerInside()
                .resize(200, 200)
                .into(binding.pickedImage)
        }
    }

    companion object {
        fun from(parent: ViewGroup): ImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemMediaPickerImageBinding.inflate(layoutInflater, parent, false)
            return ImageViewHolder(binding)
        }
    }
}

interface OnStartDragListener {
    fun onStartDrag(holder: RecyclerView.ViewHolder)
}

interface MediaPickerListener {
    fun onPickClicked(position: Int)
}

