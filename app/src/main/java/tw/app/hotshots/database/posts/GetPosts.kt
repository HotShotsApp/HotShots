package tw.app.hotshots.database.posts

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.model.main.Like
import tw.app.hotshots.model.main.Post
import tw.app.hotshots.model.media.Image

object GetPosts {
    suspend fun invoke(settings: GetPostsSettings, listener: PostsListener) {
        if (settings.getSearchType() == GetPostsSettings.Companion.Search.AllPosts) {
            getVisiblePosts(listener)
        } else {
            getUserPosts(settings.getUserUid(), listener)
        }
    }

    private suspend fun getVisiblePosts(listener: PostsListener) {
        val database = FirebaseFirestore.getInstance().collection("posts")
        val usersDatabase = FirebaseFirestore.getInstance().collection("users")
        val userUid = FirebaseAuth.getInstance().currentUser?.uid!!

        val postsList: MutableList<Post> = arrayListOf()

        val postsSnapshot = database.orderBy("createdAt", Query.Direction.DESCENDING).get().await()

        for (postDoc in postsSnapshot.documents) {
            val post = postDoc.toObject(Post::class.java)!!

            val imagesSnapshot = database.document(post.uid).collection("images").orderBy("order").get().await()

            for (imageDoc in imagesSnapshot) {
                val image = imageDoc.toObject(Image::class.java)

                post.images.add(image)
            }

            val likesSnapshot = database.document(post.uid).collection("likes").get().await()

            for (likesDoc in likesSnapshot.documents) {
                val like = likesDoc.toObject(Like::class.java)!!
                if (like.likedBy == userUid)
                    post.userLikedPost = true

                post.likes.add(like)
            }

            if (post.userUid == userUid) {
                post.user = UserSingleton.instance?.user!!
            } else {
                val userSnapshot = usersDatabase.document(post.userUid).get().await()
                val user = userSnapshot.toObject(User::class.java)!!
                post.user = user
            }

            postsList.add(post)
        }

        listener.onReceived(postsList)
    }

    private suspend fun getUserPosts(userUid: String, listener: PostsListener) {
        val database = FirebaseFirestore.getInstance().collection("posts")
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid!!

        val postsList: MutableList<Post> = arrayListOf()

        val postsSnapshot = database.whereEqualTo("userUid", userUid).orderBy("createdAt", Query.Direction.DESCENDING).get().await()

        for (postDoc in postsSnapshot.documents) {
            val post = postDoc.toObject(Post::class.java)!!

            val imagesSnapshot = database.document(post.uid).collection("images").orderBy("order").get().await()

            for (imageDoc in imagesSnapshot) {
                val image = imageDoc.toObject(Image::class.java)

                post.images.add(image)
            }

            val likesSnapshot = database.document(post.uid).collection("likes").get().await()

            for (likesDoc in likesSnapshot.documents) {
                val like = likesDoc.toObject(Like::class.java)!!
                if (like.likedBy == currentUserUid)
                    post.userLikedPost = true

                post.likes.add(like)
            }

            post.user = UserSingleton.instance?.user!!
            postsList.add(post)
        }

        listener.onReceived(postsList)
    }
}

class GetPostsSettings {
    private var _searchType = Search.AllPosts
    private var _userUid = ""

    companion object {
        enum class Search {
            AllPosts,
            UserPosts
        }
    }

    fun getSearchType(): Search {
        return _searchType
    }

    fun setSearchAllPosts(): GetPostsSettings {
        _searchType = Search.AllPosts
        return this
    }

    fun setSearchUserPosts(userUid: String): GetPostsSettings {
        _searchType = Search.UserPosts
        _userUid = userUid
        return this
    }

    fun getUserUid(): String {
        return _userUid
    }
}

interface PostsListener {
    fun onReceived(posts: MutableList<Post>)

    fun onError(e: java.lang.Exception) {
        Logger.log(
            "GetPosts | Cannot get posts from Database\n\nReason:\n${e.message}",
            LogType.CRITICAL
        )
    }
}