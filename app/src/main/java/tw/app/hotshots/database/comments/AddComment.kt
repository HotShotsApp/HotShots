package tw.app.hotshots.database.comments

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.comments.model.Comment

class AddComment {
    public suspend fun invoke(postUid: String, comment: Comment, isResponse: Boolean = false, responseTo: String = "", listener: AddCommentListener) {
        if (isResponse) {
            var result = FirebaseFirestore
                .getInstance()
                .collection(Constants.Collections.POSTS)
                .document(postUid)
                .collection(Constants.Collections.COMMENTS)
                .document(responseTo)
                .collection(Constants.Collections.RESPONSES)
                .document(comment.uid)
                .set(comment)
                .addOnSuccessListener {
                    listener.onAdded()
                }
                .addOnFailureListener {
                    listener.onError(it)
                }
                .await()
        } else {
            var result = FirebaseFirestore
                .getInstance()
                .collection(Constants.Collections.POSTS)
                .document(postUid)
                .collection(Constants.Collections.COMMENTS)
                .document(comment.uid)
                .set(comment)
                .addOnSuccessListener {
                    listener.onAdded()
                }
                .addOnFailureListener {
                    listener.onError(it)
                }
                .await()
        }
    }
}

interface AddCommentListener {
    fun onAdded()

    fun onError(exception: Exception)
}