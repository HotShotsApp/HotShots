package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.posts.model.Post

class RemovePost {
    public suspend fun invoke(post: Post, listener: RemovePostListener) {
        val removeResult = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .document(post.uid)
            .delete()
            .addOnSuccessListener {
                listener.onRemoved()
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface RemovePostListener {
    fun onRemoved()

    fun onError(exception: Exception)
}