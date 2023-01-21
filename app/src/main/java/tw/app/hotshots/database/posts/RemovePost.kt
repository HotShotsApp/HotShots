package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import tw.app.hotshots.model.main.Post

object RemovePost {
    fun invoke(post: Post, listener: RemovePostListener) {
        post.isHidden = true
        val database = FirebaseFirestore.getInstance().collection("posts").document(post.uid).set(post)
            .addOnSuccessListener {
                listener.onRemoved()
            }
            .addOnFailureListener {
                listener.onError()
            }
    }
}

interface RemovePostListener {
    fun onRemoved() {}

    fun onError() {}
}