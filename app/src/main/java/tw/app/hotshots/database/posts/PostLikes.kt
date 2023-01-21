package tw.app.hotshots.database.posts

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp


object PostLikes {
    fun LikeWatcher(postUID: String, listener: LikesListener) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        firestore.collection("posts").document(postUID).collection("likes")
            .addSnapshotListener { value, error ->
                var userLiked = false
                var i = 0
                for (likeDoc in value?.documents!!) {
                    i++
                    val like = likeDoc.toObject(tw.app.hotshots.model.main.Like::class.java)
                    if (like != null) {
                        if (like.likedBy == user?.uid!!) {
                            userLiked = true
                        }
                    }
                }

                listener.OnUpdate(userLiked, i)
            }
    }

    interface LikesListener {
        fun OnUpdate(userLiked: Boolean, likesAmount: Int)
    }


    fun Like(postUID: String) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val timeStamp = Timestamp(System.currentTimeMillis()).time

        // TODO: Make this to work with new system
        val like = tw.app.hotshots.model.main.Like(user?.uid!!)
        firestore.collection("posts").document(postUID).collection("likes").document(user.uid)
            .set(like)
        val mapAddPostToUserLiked = HashMap<String, Any>()
        mapAddPostToUserLiked["postUid"] = postUID
        mapAddPostToUserLiked["likeAt"] = timeStamp
        firestore.collection("users").document(user.uid).collection("likedPosts").document(postUID)
            .set(mapAddPostToUserLiked)
    }

    fun UnLike(postUID: String) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        firestore.collection("posts").document(postUID).collection("likes")
            .document(user!!.uid).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                documentSnapshot.reference.delete()
            }

        firestore.collection("users").document(user.uid).collection("likedPosts").document(postUID).delete()
    }
}
