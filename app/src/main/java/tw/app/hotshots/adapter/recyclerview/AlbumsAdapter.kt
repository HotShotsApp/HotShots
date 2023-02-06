package tw.app.hotshots.adapter.recyclerview

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import tw.app.hotshots.databinding.ItemAlbumBinding
import tw.app.hotshots.model.main.Album

class AlbumsAdapter(
    private val mData: MutableList<Album>
) : RecyclerView.Adapter<AlbumsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsAdapter.ViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent ,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var binding = holder.binding
        var album = mData[position]

        binding.loadingImageProgress.visibility = VISIBLE
        binding.imagesCountText.text = album.images.size.toString()

        Picasso.get()
            .load(album.coverImage)
            .into(binding.albumCoverImage, object : Callback {
                override fun onSuccess() {
                    binding.loadingImageProgress.visibility = GONE
                }

                override fun onError(e: Exception?) {

                }
            })
    }

    override fun getItemCount(): Int {
        return mData.size
    }
}

interface OnAlbumClickListener {
    fun onClick(album: Album)
}