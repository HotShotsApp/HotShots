package tw.app.hotshots.adapter.viewpager.post

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import tw.app.hotshots.adapter.viewpager.post.fragment.PostImageFragment
import tw.app.hotshots.model.media.Image

class PostImagesAdapter(
    private val mImages: MutableList<Image>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return PostImageFragment(mImages[position].imageUrl)
    }

    override fun getItemCount(): Int {
        return mImages.size
    }
}