package tw.app.hotshots.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.database.posts.model.Post
import tw.app.hotshots.databinding.ItemPostBinding
import tw.app.hotshots.settings.Settings

class PostAdapter(
    private val mData: MutableList<Post>,
    private val settings: Settings
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context))

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(mData[position]) {
                // Load basic data
                if (this.mainImageUrl.isNotBlank()) {
                    if (settings.isStrongCompressImagesInMainPageEnabled()) {
                        Picasso
                            .get()
                            .load(this.mainImageUrl)
                            .resize(Constants.ImageCompression.MAXIMUM, Constants.ImageCompression.MAXIMUM)
                            .centerCrop()
                            .into(binding.postImageView)
                    } else {
                        Picasso
                            .get()
                            .load(this.mainImageUrl)
                            .resize(Constants.ImageCompression.MINIMUM, Constants.ImageCompression.MINIMUM)
                            .centerCrop()
                            .into(binding.postImageView)
                    }
                }

                binding.descriptionTextView.text = this.description

                // Get User Data from database
                val userDocument = FirebaseFirestore
                    .getInstance()
                    .collection(Constants.Collections.USERS)
                    .document(this.createdBy)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val userObject = it.toObject(HotUser::class.java)

                            if (userObject != null) {
                                binding.usernameTextView.text = userObject.name
                            } else {
                                binding.usernameTextView.text = "Error01"
                            }
                        } else {
                            binding.usernameTextView.text = "Error02"
                        }
                    }
                    .addOnFailureListener {
                        binding.usernameTextView.text = "Error03"
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    public fun addPostsToList(posts: MutableList<Post>) {
        for (post in posts) {
            mData.add(post)
            notifyItemInserted(mData.size)
        }
    }
}