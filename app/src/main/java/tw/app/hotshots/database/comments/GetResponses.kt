package tw.app.hotshots.database.comments

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.comments.model.Comment

class GetResponses {
    public suspend fun invoke(postUid: String, commentUid: String, listener: GetResponsesListener) {
        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        val result = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .document(postUid)
            .collection(Constants.Collections.COMMENTS)
            .document(commentUid)
            .collection(Constants.Collections.RESPONSES)
            .get()
            .addOnFailureListener {
                isLastActionSuccess = false
                lastException = it
            }
            .await()

        if (!isLastActionSuccess) {
            listener.onError(lastException)
            return
        }

        if (result.isEmpty) {
            listener.onSuccess(arrayListOf())
            return
        }

        val responsesList: MutableList<Comment> = arrayListOf()
        var failedToConvert = 0
        for (doc in result.documents) {
            val obj = doc.toObject(Comment::class.java)

            if (obj != null) {
                responsesList.add(obj)
            } else {
                failedToConvert++
            }
        }

        if (failedToConvert != 0) {
            listener.onSuccess(responsesList, "Nie można przetworzyć $failedToConvert komentarzy.")
            return
        }

        listener.onSuccess(responsesList)
    }
}

interface GetResponsesListener {
    fun onSuccess(responses: MutableList<Comment>, info: String = "")

    fun onError(exception: Exception)
}