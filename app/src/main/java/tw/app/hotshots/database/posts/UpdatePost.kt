package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.posts.model.Post

class UpdatePost {
    public suspend fun invoke(post: Post, listener: UpdatePostListener) {
        val updateResult = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .document(post.uid)
            .set(post)
            .addOnSuccessListener {
                listener.onUpdated()
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface UpdatePostListener {
    fun onUpdated()

    fun onError(exception: Exception)
}