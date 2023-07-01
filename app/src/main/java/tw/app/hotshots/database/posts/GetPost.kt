package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.posts.model.Post

class GetPost {
    public suspend fun invoke(uid: String, listener: GetPostListener) {
        var isLatestActionSuccess = true
        var latestException: Exception = Exception("Unknown Error!")

        val getResult = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .document(uid)
            .get()
            .addOnFailureListener {
                isLatestActionSuccess = false
                latestException = it
            }
            .await()

        if (!isLatestActionSuccess) {
            listener.onError(latestException)
            return
        }

        if (!getResult.exists()) {
            listener.onError(Exception("Post nie istnieje!"))
            return
        }

        val postObject = getResult.toObject(Post::class.java)

        if (postObject == null) {
            listener.onError(Exception("Cannot convert document to object!"))
            return
        }

        listener.onSuccess(postObject)
    }
}

interface GetPostListener {
    fun onSuccess(post: Post)

    fun onError(exception: Exception)
}