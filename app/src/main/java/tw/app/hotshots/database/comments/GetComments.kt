package tw.app.hotshots.database.comments

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.comments.model.Comment

class GetComments {
    public suspend fun invoke(postUid: String, listener: GetCommentsListener) {
        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        val commentsDatabase = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .document(postUid)
            .collection(Constants.Collections.COMMENTS)
            .get()
            .addOnFailureListener {
                lastException = it
                isLastActionSuccess = false
            }
            .await()

        if (!isLastActionSuccess) {
            listener.onError(lastException)
            return
        }

        if (commentsDatabase.isEmpty) {
            listener.onError(Exception("Pusto!"))
            return
        }

        val commentsList: MutableList<Comment> = arrayListOf()

        // Get Comments
        for (doc in commentsDatabase.documents) {
            val objCom = doc.toObject(Comment::class.java)

            // If comment object is not null, get responses
            if (objCom != null) {
                val responsesDatabase = FirebaseFirestore
                    .getInstance()
                    .collection(Constants.Collections.POSTS)
                    .document(postUid)
                    .collection(Constants.Collections.COMMENTS)
                    .document(objCom.uid)
                    .collection(Constants.Collections.RESPONSES)
                    .get()
                    .addOnFailureListener {
                        lastException = it
                        isLastActionSuccess = false
                    }
                    .await()

                if (!responsesDatabase.isEmpty) {
                    for (resDoc in responsesDatabase.documents) {
                        val resObj = resDoc.toObject(Comment::class.java)

                        if (resObj != null) {
                            resObj.isResponse = true
                            objCom.responses.add(resObj)
                        }
                    }
                }

                commentsList.add(objCom)
            }
        }

        listener.onSuccess(commentsList)
    }
}

interface GetCommentsListener {
    fun onSuccess(comments: MutableList<Comment>)

    fun onError(exception: Exception)
}