package tw.app.hotshots.adapter.recyclerview

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import tw.app.hotshots.R
import tw.app.hotshots.databinding.ItemInfluencerBinding
import tw.app.hotshots.extensions.setImageBlackWhite
import tw.app.hotshots.model.main.Influencer

class InfluencersAdapter(
    private val mData: MutableList<Influencer>,
    private var context: Context,
    private val listener: OnInfluencerClickListener
) : RecyclerView.Adapter<InfluencersAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemInfluencerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfluencersAdapter.ViewHolder {
        val binding = ItemInfluencerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var influencer = mData[position]
        var binding = holder.binding

        binding.root.setOnClickListener {
            listener.onClick(influencer)
        }

        binding.influencerNameTextView.text = influencer.nickname
        binding.albumsCountText.text = influencer.albumsCount.toString()
        binding.followersCountText.text = influencer.follows.size.toString()

        if (influencer.isVerified)
            binding.influencerNameTextView.setTextColor(context.getColor(R.color.user_administrator))

        Picasso.get()
            .load(influencer.avatar)
            .into(binding.influencerAvatarImage)

        // Change ImageView saturation so it will display a image in grey scale
        /*val typedValue = TypedValue()
        val theme: Resources.Theme = context.theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        @ColorInt val color: Int = typedValue.data
        binding.influencerAvatarImage.setImageBlackWhite(color)*/
    }

    override fun getItemCount(): Int {
        return mData.size
    }
}

interface OnInfluencerClickListener {
    fun onClick(influencer: Influencer)
}