package tw.app.hotshots.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yy.mobile.rollingtextview.CharOrder
import com.yy.mobile.rollingtextview.strategy.Direction
import com.yy.mobile.rollingtextview.strategy.Strategy.CarryBitAnimation
import kotlinx.coroutines.CoroutineScope
import tw.app.hotshots.R
import tw.app.hotshots.adapter.viewpager.post.PostImagesAdapter
import tw.app.hotshots.database.posts.PostLikes
import tw.app.hotshots.database.posts.RemovePost
import tw.app.hotshots.database.posts.RemovePostListener
import tw.app.hotshots.database.posts.user.UserSingleton
import tw.app.hotshots.databinding.ItemPostBinding
import tw.app.hotshots.model.main.Post

/**
 * Adapter that shows available Posts
 *
 * @param mData MutableList with Posts
 * @param fragmentManager of ViewPager Host - needed to show post images
 * @param lifecycle  of ViewPager Host - needed to show post images
 */
class PostsAdapter(
    private val mData: MutableList<Post>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val lifecycle: Lifecycle
) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsAdapter.ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val post = mData[position]
        val adapter = PostImagesAdapter(post.images, fragmentManager, lifecycle)
        val currentUserUid = UserSingleton.instance?.user?.uid!!
        var userLikedPost = post.userLikedPost
        var prevLikesCount = post.likes.size

        holder.binding.likesPostAmountText.charStrategy = CarryBitAnimation(Direction.SCROLL_DOWN)
        holder.binding.likesPostAmountText.addCharOrder(CharOrder.Number)
        holder.binding.likesPostAmountText.animationInterpolator = OvershootInterpolator()

        if (post.images.size == 1)
            holder.binding.viewpagerIndicator.visibility = GONE
        else
            holder.binding.viewpagerIndicator.visibility = VISIBLE

        val popup = PopupMenu(context, holder.binding.postMoreButton)
        popup.inflate(R.menu.menu_post)

        if (post.userUid == currentUserUid)
            popup.menu.findItem(R.id.remove_post_button).isVisible = true

        popup.setOnMenuItemClickListener {
            if (it.itemId == R.id.remove_post_button) {
                var confirmDialog: AlertDialog? = MaterialAlertDialogBuilder(context)
                    .setTitle("Usuń Post")
                    .setMessage("Czy na pewno chcesz usunąć ten post?")
                    .setPositiveButton(
                        "Usuń"
                    ) { dialog, which ->
                        RemovePost.invoke(post, object : RemovePostListener {
                            override fun onRemoved() {
                                super.onRemoved()

                                mData.removeAt(position)
                                notifyItemRemoved(position)
                            }

                            override fun onError() {
                                super.onError()
                            }
                        })
                    }
                    .setNegativeButton("Anuluj", null)
                    .show()

                confirmDialog = null
            }

            true
        }

        holder.binding.postMoreButton.setOnClickListener {
            popup.show()
        }

        holder.binding.userUsernameText.text = post.user.name
        holder.binding.postDescriptionText.text = post.description
        holder.binding.likesPostAmountText.setText(post.likes.size.toString())
        holder.binding.viewpagerWithImages.adapter = adapter
        holder.binding.viewpagerIndicator.attachTo(holder.binding.viewpagerWithImages)
        holder.binding.likesPostAmountText.animationDuration = 200

        PostLikes.LikeWatcher(post.uid, object : PostLikes.LikesListener {
            override fun OnUpdate(userLiked: Boolean, likesAmount: Int) {
                userLikedPost = userLiked
                if (likesAmount > prevLikesCount) {
                    holder.binding.likesPostAmountText.charStrategy =
                        CarryBitAnimation(Direction.SCROLL_DOWN)
                } else {
                    holder.binding.likesPostAmountText.charStrategy =
                        CarryBitAnimation(Direction.SCROLL_UP)
                }
                holder.binding.likesPostAmountText.setText(likesAmount.toString())

                if (userLiked)
                    holder.binding.likePostButton.setIconResource(R.drawable.ic_favorite_filled_wb)
                else
                    holder.binding.likePostButton.setIconResource(R.drawable.ic_favorite_wb)

                holder.binding.likePostButton.isEnabled = true

                prevLikesCount = likesAmount
            }
        })

        holder.binding.likePostButton.setOnClickListener {
            holder.binding.likePostButton.isEnabled = false
            if (userLikedPost) {
                PostLikes.UnLike(post.uid)
            } else {
                PostLikes.Like(post.uid)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

}