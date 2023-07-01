package tw.app.hotshots.database.posts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.database.posts.model.Post

class ListenPosts {

    public suspend fun invoke(listener: ListenPostsListener) {
        var freshPostsList: MutableList<Post> = arrayListOf()

        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        val firstResult = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
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

        if (!firstResult.isEmpty) {
            for (doc in firstResult.documents) {
                val obj = doc.toObject(Post::class.java)

                if (obj != null) {
                    freshPostsList.add(obj)
                }
            }

            listener.onFullReceived(freshPostsList)
            freshPostsList = arrayListOf()
        }

        var newPostsList: MutableList<Post> = arrayListOf()

        val result = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.POSTS)
            .addSnapshotListener { value, error ->
                if (value == null) {
                    if (error == null) {
                        listener.onError(Exception("Unknown Error Occurred!"))
                    } else {
                        listener.onError(error)
                    }
                } else {
                    if (!value.isEmpty) {
                        for (doc in value.documents) {
                            val postObj = doc.toObject(Post::class.java)

                            if (postObj != null) {
                                newPostsList.add(postObj)
                            }
                        }

                        listener.onNewPostsAdded(newPostsList)
                        newPostsList = arrayListOf()
                    }
                }
            }
    }

}

interface ListenPostsListener {
    /**
     * This function triggers when all, already created posts are received.
     */
    fun onFullReceived(posts: MutableList<Post>)

    /**
     * This function triggers when new posts are created, but they are not added to list or triggered by onFullReceived function.
     *
     * User will be notified when new posts will be added.
     */
    fun onNewPostsAdded(newPosts: MutableList<Post>)

    fun onError(exception: Exception)
}