package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.posts.model.Post

class AddPost {
    public suspend fun invoke(post: Post, listener: AddPostListener) {
        val upload = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .document(post.uid)
            .set(post)
            .addOnSuccessListener {
                listener.onAdded()
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface AddPostListener {
    fun onAdded()

    fun onError(exception: Exception)
}