package tw.app.hotshots.database.comments

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants

class RemoveComment {
    public suspend fun invoke(postUid: String, commentUid: String, isResponse: Boolean = false, respondedTo: String = "", listener: RemoveCommentListener) {
        if (isResponse) {
            val actionResult = FirebaseFirestore
                .getInstance()
                .collection(Constants.Collections.POSTS)
                .document(postUid)
                .collection(Constants.Collections.COMMENTS)
                .document(respondedTo)
                .collection(Constants.Collections.RESPONSES)
                .document(commentUid)
                .delete()
                .addOnSuccessListener {
                    listener.onRemoved()
                }
                .addOnFailureListener {
                    listener.onError(it)
                }
                .await()
        } else {
            val actionResult = FirebaseFirestore
                .getInstance()
                .collection(Constants.Collections.POSTS)
                .document(postUid)
                .collection(Constants.Collections.COMMENTS)
                .document(commentUid)
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
}

interface RemoveCommentListener {
    fun onRemoved()

    fun onError(exception: Exception)
}