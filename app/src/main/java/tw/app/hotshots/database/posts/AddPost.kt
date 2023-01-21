package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.model.main.Post

object AddPost {
    suspend fun invoke(post: Post, listener: AddPostListener) {
        val database = FirebaseFirestore.getInstance().collection("posts").document(post.uid)

        database
            .set(post)
            .addOnFailureListener {
                listener.onError(it)
                return@addOnFailureListener
            }.await()

        for (image in post.images) {
            database.collection("images")
                .document(image.uid)
                .set(image)
                .addOnFailureListener {
                    listener.onError(it)
                    return@addOnFailureListener
                }
                .await()
        }

        listener.onAdded()
    }
}

interface AddPostListener {
    fun onAdded()

    fun onError(e: java.lang.Exception) {
        Logger.log(
            "AddPost | Cannot add post to Database\n\nReason:\n${e.message}",
            LogType.CRITICAL
        )
    }
}